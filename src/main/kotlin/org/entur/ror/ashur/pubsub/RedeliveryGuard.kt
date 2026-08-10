package org.entur.ror.ashur.pubsub

import org.entur.ror.ashur.config.AppConfig
import org.entur.ror.ashur.file.AshurBucketService
import org.entur.ror.ashur.file.ClaimStore
import org.entur.ror.ashur.metrics.FilterMetrics
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import tools.jackson.module.kotlin.jacksonObjectMapper

/** What the caller should do with this delivery. */
enum class GuardDecision { SKIP, PROCESS, BOUNCE }

/** The minimal, message-derived facts the guard needs. [outputExists] is checked via [outputPath]. */
data class GuardRequest(val codespace: String, val correlationId: String, val outputPath: String)

/**
 * Claim-based guard against duplicate/redundant processing of the same filter request. Runs on every
 * delivery, before any work. Pub/Sub is at-least-once, so the same request can be delivered more than
 * once; without this guard each delivery re-ran the whole pipeline (wasted compute, a re-sent
 * SUCCEEDED, double-counted metrics).
 *
 * The delivery situations it distinguishes:
 * - **First delivery of a request** — no output, no claim: we claim it and process. The normal path.
 * - **Redelivery after the request already finished** (sequential duplicate) — the output artifact
 *   already exists: skip. Happens when the original run completed but its message was redelivered
 *   anyway (e.g. it finished right around the ack deadline, or the pod crashed *after* uploading the
 *   output but before the ack).
 * - **Redelivery while the original is still in flight** (concurrent duplicate) — a *fresh* claim
 *   exists: bounce and let it be redelivered later. Happens when a run outlives the 600 s ack
 *   deadline, so Pub/Sub redelivers it onto the other pod while the first pod is still filtering.
 * - **Redelivery after the holder died mid-run** (crash recovery) — a *stale* claim exists: take it
 *   over and process. Happens when the pod holding the claim was killed (OOM, eviction, deploy
 *   SIGKILL) before finishing, so it never wrote the output and never got to ack.
 *
 * Invariant: a delivery is turned into an ack only when the work is genuinely done or proven done
 * (output exists / we own the claim). "Someone else is (or was recently) working on it" is a
 * [GuardDecision.BOUNCE] (nack), never an ack — so a crashed holder never causes an ack, and
 * redeliveries keep coming until the claim goes stale and a redelivery takes it over.
 */
@Component
class RedeliveryGuard(
    private val claimStore: ClaimStore,
    private val ashurBucketService: AshurBucketService,
    private val filterMetrics: FilterMetrics,
    appConfig: AppConfig,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val mapper = jacksonObjectMapper()

    private val enabled = appConfig.redeliveryGuard.enabled
    private val ttlMs = appConfig.redeliveryGuard.ttlSeconds * 1000
    private val owner: String = System.getenv("HOSTNAME") ?: "unknown"

    fun evaluate(request: GuardRequest, nowEpochMs: Long = System.currentTimeMillis()): GuardDecision {
        if (!enabled) return GuardDecision.PROCESS

        val codespace = request.codespace
        val correlationId = request.correlationId

        // Done-signal: the request already succeeded on an earlier delivery and its output is on the
        // bucket. This is the sequential-duplicate case — a redelivery that arrived after completion.
        // Nothing left to do, so ack and skip (no re-run, no STARTED/SUCCEEDED, no metrics).
        if (ashurBucketService.exists(request.outputPath)) {
            logger.info(
                "Redelivery guard: output already exists for codespace={} correlationId={} at {}; skipping (ack, no re-processing).",
                codespace, correlationId, request.outputPath,
            )
            filterMetrics.recordGuardOutcome(FilterMetrics.GUARD_OUTCOME_SKIPPED_DONE, codespace)
            return GuardDecision.SKIP
        }

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
            GuardDecision.PROCESS
        }
    }

    private fun decideOnClaim(claimPath: String, codespace: String, correlationId: String, nowEpochMs: Long): GuardDecision {
        // We atomically win the claim iff no one else holds one. This is the normal first-delivery
        // case: no other pod is (or was) working on this request, so we own it and process.
        val created = claimStore.createIfAbsent(claimPath, serialize(Claim(owner, nowEpochMs, attempt = 1)))
        if (created) {
            logger.info("Redelivery guard: claimed codespace={} correlationId={}; processing.", codespace, correlationId)
            filterMetrics.recordGuardOutcome(FilterMetrics.GUARD_OUTCOME_CLAIMED, codespace)
            return GuardDecision.PROCESS
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
            return GuardDecision.BOUNCE
        }

        val claim = deserialize(existing.content)
        val ageMs = nowEpochMs - claim.startedAtEpochMs
        // A claim exists and is still young (< TTL). We deliberately do NOT take it over yet: the
        // holder is presumed alive. This covers two situations, and bouncing is right for both:
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
            return GuardDecision.BOUNCE
        }

        // The claim is stale (age >= TTL) yet the output still doesn't exist. The previous holder is
        // presumed dead: it claimed the request, then was killed mid-run (OOM, eviction, deploy
        // SIGKILL) before writing the output — otherwise the done-signal above would have fired. We
        // take over the abandoned work. The overwrite is conditional on the generation we read, so if
        // two pods both spot the same stale claim only one wins the compare-and-swap.
        // Caveat: a *genuinely* slow-but-alive run that exceeds the TTL is indistinguishable from a
        // crash and gets taken over too — a bounded, safe duplicate (both write the same object;
        // last-writer-wins). TTL must therefore exceed the worst-case legitimate run duration.
        val tookOver = claimStore.overwriteIfGeneration(
            claimPath,
            serialize(Claim(owner, nowEpochMs, attempt = claim.attempt + 1)),
            existing.generation,
        )
        return if (tookOver) {
            logger.info(
                "Redelivery guard: took over stale claim (age={}ms >= ttl={}ms, prevOwner={}) for codespace={} correlationId={}; processing.",
                ageMs, ttlMs, claim.owner, codespace, correlationId,
            )
            filterMetrics.recordGuardOutcome(FilterMetrics.GUARD_OUTCOME_TOOK_OVER_STALE, codespace)
            GuardDecision.PROCESS
        } else {
            // Both pods saw the same stale claim and raced to take it over; the compare-and-swap let
            // the other pod win, so it is now processing. Bounce (nack) — the winner owns the work.
            logger.info(
                "Redelivery guard: lost the takeover race for codespace={} correlationId={}; bouncing (nack).",
                codespace, correlationId,
            )
            filterMetrics.recordGuardOutcome(FilterMetrics.GUARD_OUTCOME_BOUNCED_FRESH, codespace)
            GuardDecision.BOUNCE
        }
    }

    private fun serialize(claim: Claim): ByteArray = mapper.writeValueAsBytes(claim)
    private fun deserialize(bytes: ByteArray): Claim = mapper.readValue(bytes, Claim::class.java)
}
