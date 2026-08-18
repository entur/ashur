package org.entur.ror.ashur.pubsub

import com.google.cloud.storage.StorageException
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.entur.ror.ashur.config.AppConfig
import org.entur.ror.ashur.file.ClaimStore
import org.entur.ror.ashur.file.InMemoryClaimStore
import org.entur.ror.ashur.filter.FilterProfile
import org.entur.ror.ashur.metrics.FilterMetrics
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import tools.jackson.module.kotlin.jacksonObjectMapper
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RedeliveryGuardTest {

    private val ttlSeconds = 1200L
    private val ttlMs = ttlSeconds * 1000
    private val claimPath = "claims/RUT/corr-1/StandardImportFilter"
    private val request = GuardRequest(
        codespace = "RUT",
        correlationId = "corr-1",
        filterProfile = FilterProfile.StandardImportFilter,
    )
    private val mapper = jacksonObjectMapper()

    private fun appConfig(enabled: Boolean = true) = AppConfig(
        redeliveryGuard = AppConfig.RedeliveryGuardConfig().also {
            it.enabled = enabled
            it.ttlSeconds = ttlSeconds
        }
    )

    private fun guard(claimStore: ClaimStore, enabled: Boolean = true): Pair<RedeliveryGuard, SimpleMeterRegistry> {
        val registry = SimpleMeterRegistry()
        val guard = RedeliveryGuard(claimStore, FilterMetrics(registry), appConfig(enabled))
        return guard to registry
    }

    private fun guardCount(registry: SimpleMeterRegistry, outcome: String) =
        registry.find(FilterMetrics.GUARD_METRIC_NAME).tags("outcome", outcome, "codespace", "RUT")
            .counter()?.count() ?: 0.0

    private fun put(store: InMemoryClaimStore, claim: Claim) = store.put(claimPath, mapper.writeValueAsBytes(claim))

    @Test
    fun `claims and processes when no existing claim`() {
        val store = InMemoryClaimStore()
        val (guard, registry) = guard(store)

        val result = guard.evaluate(request, nowEpochMs = 1_000)

        assertEquals(GuardDecision.PROCESS, result.decision)
        assertNotNull(result.claimHandle)
        assertEquals(claimPath, result.claimHandle!!.path)
        assertEquals(1.0, guardCount(registry, FilterMetrics.GUARD_OUTCOME_CLAIMED))
    }

    @Test
    fun `bounces when a fresh, not-yet-completed claim already exists`() {
        val store = InMemoryClaimStore()
        put(store, Claim(owner = "pod-a", startedAtEpochMs = 1_000, attempt = 1))
        val (guard, registry) = guard(store)

        // now is only 10s after the claim started; well under the 1200s TTL.
        val result = guard.evaluate(request, nowEpochMs = 11_000)

        assertEquals(GuardDecision.BOUNCE, result.decision)
        assertNull(result.claimHandle)
        assertEquals(1.0, guardCount(registry, FilterMetrics.GUARD_OUTCOME_BOUNCED_FRESH))
    }

    @Test
    fun `takes over and processes when the existing claim is stale and not completed`() {
        val store = InMemoryClaimStore()
        put(store, Claim(owner = "pod-a", startedAtEpochMs = 1_000, attempt = 1))
        val (guard, registry) = guard(store)

        val now = 1_000 + ttlMs + 1 // just past the TTL
        val result = guard.evaluate(request, nowEpochMs = now)

        assertEquals(GuardDecision.PROCESS, result.decision)
        assertNotNull(result.claimHandle)
        assertEquals(1.0, guardCount(registry, FilterMetrics.GUARD_OUTCOME_TOOK_OVER_STALE))

        // the claim was overwritten with a bumped attempt and refreshed start time
        val rewritten = mapper.readValue(store.read(claimPath)!!.content, Claim::class.java)
        assertEquals(2, rewritten.attempt)
        assertEquals(now, rewritten.startedAtEpochMs)
        assertEquals(false, rewritten.completed)
    }

    @Test
    fun `skips when the existing claim is marked completed, even if fresh`() {
        val store = InMemoryClaimStore()
        put(store, Claim(owner = "pod-a", startedAtEpochMs = 1_000, attempt = 1, completed = true))
        val (guard, registry) = guard(store)

        // Still well within the TTL window, yet completed=true must win over freshness.
        val result = guard.evaluate(request, nowEpochMs = 11_000)

        assertEquals(GuardDecision.SKIP, result.decision)
        assertNull(result.claimHandle)
        assertEquals(1.0, guardCount(registry, FilterMetrics.GUARD_OUTCOME_SKIPPED_DONE))
    }

    @Test
    fun `skips when the existing claim is marked completed and stale`() {
        // This is the regression this rewrite fixes: a genuinely finished run's claim eventually goes
        // stale by age, but must never be taken over and reprocessed.
        val store = InMemoryClaimStore()
        put(store, Claim(owner = "pod-a", startedAtEpochMs = 1_000, attempt = 1, completed = true))
        val (guard, registry) = guard(store)

        val now = 1_000 + ttlMs + 1 // well past the TTL
        val result = guard.evaluate(request, nowEpochMs = now)

        assertEquals(GuardDecision.SKIP, result.decision)
        assertNull(result.claimHandle)
        assertEquals(1.0, guardCount(registry, FilterMetrics.GUARD_OUTCOME_SKIPPED_DONE))
    }

    @Test
    fun `no-op processes when the guard is disabled and returns no claim handle`() {
        val store = InMemoryClaimStore()
        val (guard, registry) = guard(store, enabled = false)

        val result = guard.evaluate(request, nowEpochMs = 1_000)

        assertEquals(GuardDecision.PROCESS, result.decision)
        assertNull(result.claimHandle)
        assertEquals(0.0, guardCount(registry, FilterMetrics.GUARD_OUTCOME_CLAIMED))
    }

    @Test
    fun `fails open and processes when the claim store throws`() {
        val registry = SimpleMeterRegistry()
        val throwingStore = mock<ClaimStore> {
            on { createIfAbsent(any(), any()) } doThrow StorageException(503, "unavailable")
        }
        val guard = RedeliveryGuard(throwingStore, FilterMetrics(registry), appConfig())

        val result = guard.evaluate(request, nowEpochMs = 1_000)

        assertEquals(GuardDecision.PROCESS, result.decision)
        assertNull(result.claimHandle)
        assertEquals(1.0, guardCount(registry, FilterMetrics.GUARD_OUTCOME_FAIL_OPEN))
    }

    @Test
    fun `markCompleted persists completed=true so a later evaluate skips`() {
        val store = InMemoryClaimStore()
        val (guard, _) = guard(store)

        val claimed = guard.evaluate(request, nowEpochMs = 1_000)
        guard.markCompleted(claimed.claimHandle!!)

        val after = guard.evaluate(request, nowEpochMs = 2_000)
        assertEquals(GuardDecision.SKIP, after.decision)

        val stored = mapper.readValue(store.read(claimPath)!!.content, Claim::class.java)
        assertTrue(stored.completed)
    }

    @Test
    fun `a second filter profile for the same codespace and correlationId is not skipped`() {
        // Marduk sends both StandardImportFilter and IncludeBlocksAndRestrictedJourneysFilter under one
        // correlationId for the same import run. They are distinct units of work producing distinct
        // artifacts, so completing one must never make the guard skip the other.
        val store = InMemoryClaimStore()
        val (guard, _) = guard(store)

        val standard = guard.evaluate(request, nowEpochMs = 1_000)
        assertEquals(GuardDecision.PROCESS, standard.decision)
        guard.markCompleted(standard.claimHandle!!)

        val blocks = guard.evaluate(
            request.copy(filterProfile = FilterProfile.IncludeBlocksAndRestrictedJourneysFilter),
            nowEpochMs = 2_000,
        )

        assertEquals(GuardDecision.PROCESS, blocks.decision)
        assertNotNull(blocks.claimHandle)
        assertEquals(
            "claims/RUT/corr-1/IncludeBlocksAndRestrictedJourneysFilter",
            blocks.claimHandle!!.path,
        )
    }

    @Test
    fun `markCompleted silently no-ops when the claim was taken over in the meantime`() {
        val store = InMemoryClaimStore()
        val (guard, _) = guard(store)

        val claimed = guard.evaluate(request, nowEpochMs = 1_000)

        // Someone else took the claim over as stale in between (generation moved on).
        val now = 1_000 + ttlMs + 1
        put(store, Claim(owner = "other-pod", startedAtEpochMs = now, attempt = 2))

        // The original (slow) holder finally finishes and tries to mark its stale handle completed.
        guard.markCompleted(claimed.claimHandle!!)

        // The new holder's claim must not be clobbered back to completed=true by the old handle.
        val stored = mapper.readValue(store.read(claimPath)!!.content, Claim::class.java)
        assertEquals("other-pod", stored.owner)
        assertEquals(false, stored.completed)
    }
}
