package org.entur.ror.ashur.pubsub

import com.google.cloud.storage.StorageException
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.entur.ror.ashur.config.AppConfig
import org.entur.ror.ashur.file.AshurBucketService
import org.entur.ror.ashur.file.ClaimStore
import org.entur.ror.ashur.file.InMemoryClaimStore
import org.entur.ror.ashur.metrics.FilterMetrics
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import tools.jackson.module.kotlin.jacksonObjectMapper
import kotlin.test.Test
import kotlin.test.assertEquals

class RedeliveryGuardTest {

    private val ttlSeconds = 1200L
    private val ttlMs = ttlSeconds * 1000
    private val outputPath = "RUT/corr-1/filtered_data.zip"
    private val claimPath = "claims/RUT/corr-1"
    private val request = GuardRequest(codespace = "RUT", correlationId = "corr-1", outputPath = outputPath)
    private val mapper = jacksonObjectMapper()

    private fun appConfig(enabled: Boolean = true) = AppConfig(
        redeliveryGuard = AppConfig.RedeliveryGuardConfig().also {
            it.enabled = enabled
            it.ttlSeconds = ttlSeconds
        }
    )

    private fun guard(
        claimStore: ClaimStore,
        outputExists: Boolean,
        enabled: Boolean = true,
    ): Pair<RedeliveryGuard, SimpleMeterRegistry> {
        val registry = SimpleMeterRegistry()
        val bucket = mock<AshurBucketService> { on { exists(outputPath) } doReturn outputExists }
        val guard = RedeliveryGuard(claimStore, bucket, FilterMetrics(registry), appConfig(enabled))
        return guard to registry
    }

    private fun guardCount(registry: SimpleMeterRegistry, outcome: String) =
        registry.find(FilterMetrics.GUARD_METRIC_NAME).tags("outcome", outcome, "codespace", "RUT")
            .counter()?.count() ?: 0.0

    @Test
    fun `skips when the output already exists`() {
        val store = InMemoryClaimStore()
        val (guard, registry) = guard(store, outputExists = true)

        assertEquals(GuardDecision.SKIP, guard.evaluate(request, nowEpochMs = 1_000))
        assertEquals(1.0, guardCount(registry, FilterMetrics.GUARD_OUTCOME_SKIPPED_DONE))
    }

    @Test
    fun `claims and processes when no output and no existing claim`() {
        val store = InMemoryClaimStore()
        val (guard, registry) = guard(store, outputExists = false)

        assertEquals(GuardDecision.PROCESS, guard.evaluate(request, nowEpochMs = 1_000))
        assertEquals(1.0, guardCount(registry, FilterMetrics.GUARD_OUTCOME_CLAIMED))
    }

    @Test
    fun `bounces when a fresh claim already exists`() {
        val store = InMemoryClaimStore()
        store.put(claimPath, mapper.writeValueAsBytes(Claim(owner = "pod-a", startedAtEpochMs = 1_000, attempt = 1)))
        val (guard, registry) = guard(store, outputExists = false)

        // now is only 10s after the claim started; well under the 1200s TTL.
        assertEquals(GuardDecision.BOUNCE, guard.evaluate(request, nowEpochMs = 11_000))
        assertEquals(1.0, guardCount(registry, FilterMetrics.GUARD_OUTCOME_BOUNCED_FRESH))
    }

    @Test
    fun `takes over and processes when the existing claim is stale`() {
        val store = InMemoryClaimStore()
        store.put(claimPath, mapper.writeValueAsBytes(Claim(owner = "pod-a", startedAtEpochMs = 1_000, attempt = 1)))
        val (guard, registry) = guard(store, outputExists = false)

        val now = 1_000 + ttlMs + 1 // just past the TTL
        assertEquals(GuardDecision.PROCESS, guard.evaluate(request, nowEpochMs = now))
        assertEquals(1.0, guardCount(registry, FilterMetrics.GUARD_OUTCOME_TOOK_OVER_STALE))

        // the claim was overwritten with a bumped attempt and refreshed start time
        val rewritten = mapper.readValue(store.read(claimPath)!!.content, Claim::class.java)
        assertEquals(2, rewritten.attempt)
        assertEquals(now, rewritten.startedAtEpochMs)
    }

    @Test
    fun `no-op processes when the guard is disabled and does not skip on existing output`() {
        val store = InMemoryClaimStore()
        // outputExists=true would normally SKIP; disabled must still PROCESS.
        val (guard, registry) = guard(store, outputExists = true, enabled = false)

        assertEquals(GuardDecision.PROCESS, guard.evaluate(request, nowEpochMs = 1_000))
        assertEquals(0.0, guardCount(registry, FilterMetrics.GUARD_OUTCOME_SKIPPED_DONE))
    }

    @Test
    fun `fails open and processes when the claim store throws`() {
        val registry = SimpleMeterRegistry()
        val bucket = mock<AshurBucketService> { on { exists(outputPath) } doReturn false }
        val throwingStore = mock<ClaimStore> {
            on { createIfAbsent(any(), any()) } doThrow StorageException(503, "unavailable")
        }
        val guard = RedeliveryGuard(throwingStore, bucket, FilterMetrics(registry), appConfig())

        assertEquals(GuardDecision.PROCESS, guard.evaluate(request, nowEpochMs = 1_000))
        assertEquals(1.0, guardCount(registry, FilterMetrics.GUARD_OUTCOME_FAIL_OPEN))
    }
}
