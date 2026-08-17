package org.entur.ror.ashur.pubsub

import org.entur.ror.ashur.config.AppConfig
import org.entur.ror.ashur.file.ClaimStore
import org.entur.ror.ashur.metrics.FilterMetrics
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import tools.jackson.module.kotlin.jacksonObjectMapper

/** What the caller should do with this delivery. */
enum class GuardDecision { SKIP, PROCESS, BOUNCE }

/** The minimal, message-derived facts the guard needs to locate the claim. */
data class GuardRequest(val codespace: String, val correlationId: String)

/**
 * Opaque handle to the claim a [GuardDecision.PROCESS] outcome now owns. The caller must pass this
 * to [RedeliveryGuard.markCompleted] once — and only once — every externally-visible effect of the
 * run (bucket upload, exchange-bucket copy, status publish) has actually happened.
 */
data class ClaimHandle(val path: String, val generation: Long, val claim: Claim)

data class GuardResult(val decision: GuardDecision, val claimHandle: ClaimHandle? = null)

/**
 * Claim-based guard against duplicate/redundant processing of the same filter request. Runs on every
 * delivery, before any work. Pub/Sub is at-least-once, so the same request can be delivered more than
 * once; without this guard each delivery re-ran the whole pipeline (wasted compute, a re-sent
 * SUCCEEDED, double-counted metrics).
 *
 * The delivery situations it distinguishes:
 * - **First delivery of a request** — no claim: we claim it and process. The normal path.
 * - **Redelivery after the request already finished** (sequential duplicate) — the claim is marked
 *   [Claim.completed]: skip. Happens when the original run completed but its message was redelivered
 *   anyway (e.g. it finished right around the ack deadline).
 * - **Redelivery while the original is still in flight** (concurrent duplicate) — a *fresh*,
 *   not-yet-completed claim exists: bounce and let it be redelivered later. Happens when a run
 *   outlives the 600 s ack deadline, so Pub/Sub redelivers it onto the other pod while the first pod
 *   is still filtering.
 * - **Redelivery after the holder died mid-run** (crash recovery) — a *stale*, not-yet-completed
 *   claim exists: take it over and process. Happens when the pod holding the claim was killed (OOM,
 *   eviction, deploy SIGKILL) before finishing, so it never completed and never got to ack.
 *
 * Completion is deliberately NOT inferred from a side effect such as the output object existing:
 * a crash between writing that object and finishing the rest of the pipeline (exchange-bucket copy,
 * status publish) would otherwise look identical to a genuinely finished run and get permanently
 * skipped from then on. [markCompleted] is the only thing that can make a claim [Claim.completed],
 * and callers must only invoke it after the whole pipeline has actually finished.
 *
 * Invariant: a delivery is turned into an ack only when the work is genuinely done (claim completed)
 * or we now own the claim ourselves. "Someone else is (or was recently) working on it" is a
 * [GuardDecision.BOUNCE] (nack), never an ack — so a crashed holder never causes an ack, and
 * redeliveries keep coming until the claim goes stale and a redelivery takes it over.
 */
@Component
class RedeliveryGuard(
    private val claimStore: ClaimStore,
    private val filterMetrics: FilterMetrics,
    appConfig: AppConfig,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val mapper = jacksonObjectMapper()

    private val enabled = appConfig.redeliveryGuard.enabled
    private val ttlMs = appConfig.redeliveryGuard.ttlSeconds * 1000
    private val owner: String = System.getenv("HOSTNAME") ?: "unknown"

    fun evaluate(request: GuardRequest, nowEpochMs: Long = System.currentTimeMillis()): GuardResult {
        if (!enabled) return GuardResult(GuardDecision.PROCESS)

        val codespace = request.codespace
        val correlationId = request.correlationId
        val claimPath = "claims/$codespace/$correlationId"

        return try {
            decideOnClaim(claimPath, codespace, correlationId, nowEpochMs)
        } catch (e: Exception) {
            // Fail-open: the guard is a safety-net, not a correctness gate. A claim I/O error must
            // degrade to today's behavior (process, tolerate a possible duplicate), never block.
            logger.warn(
                "Redelivery guard: claim I/O failed for codespace={} correlationId={}; failing open and processing anyway.",
                codespace, correlationId, e,
            )
            filterMetrics.recordGuardOutcome(FilterMetrics.GUARD_OUTCOME_FAIL_OPEN, codespace)
            GuardResult(GuardDecision.PROCESS)
        }
    }

    private fun decideOnClaim(claimPath: String, codespace: String, correlationId: String, nowEpochMs: Long): GuardResult {
        // We atomically win the claim iff no one else holds one. This is the normal first-delivery
        // case: no other pod is (or was) working on this request, so we own it and process.
        val newClaim = Claim(owner, nowEpochMs, attempt = 1)
        val generation = claimStore.createIfAbsent(claimPath, serialize(newClaim))
        if (generation != null) {
            logger.info("Redelivery guard: claimed codespace={} correlationId={}; processing.", codespace, correlationId)
            filterMetrics.recordGuardOutcome(FilterMetrics.GUARD_OUTCOME_CLAIMED, codespace)
            return GuardResult(GuardDecision.PROCESS, ClaimHandle(claimPath, generation, newClaim))
        }

        val existing = claimStore.read(claimPath)
        if (existing == null) {
            // Effectively impossible: nothing deletes claims in-app and the lifecycle rule only
            // touches 7-day-old objects. Treat a vanished claim as a lost race and bounce; a clean
            // redelivery will re-attempt the claim.
            logger.warn(
                "Redelivery guard: claim for codespace={} correlationId={} vanished after a create conflict; bouncing.",
                codespace, correlationId,
            )
            filterMetrics.recordGuardOutcome(FilterMetrics.GUARD_OUTCOME_BOUNCED_FRESH, codespace)
            return GuardResult(GuardDecision.BOUNCE)
        }

        val claim = deserialize(existing.content)

        // Done-signal: the request already fully finished on an earlier delivery (bucket upload,
        // exchange-bucket copy, and status publish all happened) and this is a sequential-duplicate
        // redelivery arriving after the fact. Nothing left to do: ack and skip. This holds no matter
        // how stale the claim's age is — a completed claim is never taken over.
        if (claim.completed) {
            logger.info(
                "Redelivery guard: claim for codespace={} correlationId={} is completed; skipping (ack, no re-processing).",
                codespace, correlationId,
            )
            filterMetrics.recordGuardOutcome(FilterMetrics.GUARD_OUTCOME_SKIPPED_DONE, codespace)
            return GuardResult(GuardDecision.SKIP)
        }

        val ageMs = nowEpochMs - claim.startedAtEpochMs
        // A claim exists, isn't completed, and is still young (< TTL). We deliberately do NOT take it
        // over yet: the holder is presumed alive. This covers two situations, and bouncing is right
        // for both:
        //  - the other pod is genuinely still filtering (a run that outlived the 600 s ack deadline
        //    got redelivered onto us) — let it finish;
        //  - the holder crashed only moments ago, so its claim hasn't aged into "stale" yet — wait it
        //    out rather than start a racing second run.
        // Either way we bounce (nack) so Pub/Sub redelivers later: if the holder finishes, the next
        // delivery hits the done-signal above and skips; if it really crashed, the claim eventually
        // crosses the TTL and a later redelivery takes it over (see below). We never ack here.
        if (ageMs < ttlMs) {
            logger.info(
                "Redelivery guard: fresh claim (age={}ms < ttl={}ms, owner={}) for codespace={} correlationId={}; bouncing (nack) for redelivery.",
                ageMs, ttlMs, claim.owner, codespace, correlationId,
            )
            filterMetrics.recordGuardOutcome(FilterMetrics.GUARD_OUTCOME_BOUNCED_FRESH, codespace)
            return GuardResult(GuardDecision.BOUNCE)
        }

        // The claim is stale (age >= TTL) and still not completed. The previous holder is presumed
        // dead: it claimed the request, then was killed mid-run (OOM, eviction, deploy SIGKILL) before
        // finishing — otherwise the done-signal above would have fired. We take over the abandoned
        // work. The overwrite is conditional on the generation we read, so if two pods both spot the
        // same stale claim only one wins the compare-and-swap.
        // Caveat: a *genuinely* slow-but-alive run that exceeds the TTL is indistinguishable from a
        // crash and gets taken over too — a bounded, safe duplicate (both eventually write the same
        // object and try to complete; last-writer-wins). TTL must therefore exceed the worst-case
        // legitimate run duration.
        val tookOverClaim = Claim(owner, nowEpochMs, attempt = claim.attempt + 1)
        val tookOverGeneration = claimStore.overwriteIfGeneration(claimPath, serialize(tookOverClaim), existing.generation)
        return if (tookOverGeneration != null) {
            logger.info(
                "Redelivery guard: took over stale claim (age={}ms >= ttl={}ms, prevOwner={}) for codespace={} correlationId={}; processing.",
                ageMs, ttlMs, claim.owner, codespace, correlationId,
            )
            filterMetrics.recordGuardOutcome(FilterMetrics.GUARD_OUTCOME_TOOK_OVER_STALE, codespace)
            GuardResult(GuardDecision.PROCESS, ClaimHandle(claimPath, tookOverGeneration, tookOverClaim))
        } else {
            // Both pods saw the same stale claim and raced to take it over; the compare-and-swap let
            // the other pod win, so it is now processing. Bounce (nack) — the winner owns the work.
            logger.info(
                "Redelivery guard: lost the takeover race for codespace={} correlationId={}; bouncing (nack).",
                codespace, correlationId,
            )
            filterMetrics.recordGuardOutcome(FilterMetrics.GUARD_OUTCOME_BOUNCED_FRESH, codespace)
            GuardResult(GuardDecision.BOUNCE)
        }
    }

    /**
     * Marks the claim behind [handle] as completed — call only after every externally-visible effect
     * of the run has actually happened. A future [evaluate] then sees `completed=true` and SKIPs for
     * good, regardless of how stale the claim gets.
     *
     * If the generation has moved on (someone else took the claim over, presumably because this run
     * outran the TTL), the write is silently dropped: the new holder now owns completion, and this run
     * finishing late is a harmless, already-accepted duplicate (last-writer-wins on the output object).
     */
    fun markCompleted(handle: ClaimHandle) {
        val newGeneration = claimStore.overwriteIfGeneration(handle.path, serialize(handle.claim.copy(completed = true)), handle.generation)
        if (newGeneration == null) {
            logger.warn(
                "Redelivery guard: could not mark claim {} completed — its generation moved on (taken over as stale); the new holder owns completion.",
                handle.path,
            )
        }
    }

    private fun serialize(claim: Claim): ByteArray = mapper.writeValueAsBytes(claim)
    private fun deserialize(bytes: ByteArray): Claim = mapper.readValue(bytes, Claim::class.java)
}
