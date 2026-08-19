package org.entur.ror.ashur.metrics

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals

class FilterMetricsTest {
    private fun counterFor(
        registry: SimpleMeterRegistry,
        status: String,
        codespace: String,
    ): Double =
        registry.find(FilterMetrics.RUNS_METRIC_NAME)
            .tags("status", status, "codespace", codespace)
            .counter()
            ?.count() ?: 0.0

    private fun durationCount(registry: SimpleMeterRegistry, codespace: String): Long =
        registry.find(FilterMetrics.DURATION_METRIC_NAME)
            .tags("codespace", codespace)
            .timer()
            ?.count() ?: 0L

    private fun durationTotalMillis(registry: SimpleMeterRegistry, codespace: String): Double =
        registry.find(FilterMetrics.DURATION_METRIC_NAME)
            .tags("codespace", codespace)
            .timer()
            ?.totalTime(TimeUnit.MILLISECONDS) ?: 0.0

    private fun serviceJourneysKept(registry: SimpleMeterRegistry, codespace: String): Double? =
        registry.find(FilterMetrics.SERVICE_JOURNEYS_KEPT_METRIC_NAME)
            .tags("codespace", codespace)
            .gauge()
            ?.value()

    private fun serviceJourneysKeptUpdatedMs(registry: SimpleMeterRegistry, codespace: String): Double? =
        registry.find(FilterMetrics.SERVICE_JOURNEYS_KEPT_UPDATED_MS_METRIC_NAME)
            .tags("codespace", codespace)
            .gauge()
            ?.value()

    private fun serviceJourneysOriginal(registry: SimpleMeterRegistry, codespace: String): Double? =
        registry.find(FilterMetrics.SERVICE_JOURNEYS_ORIGINAL_METRIC_NAME)
            .tags("codespace", codespace)
            .gauge()
            ?.value()

    @Test
    fun `incrementSuccessfulRun records one run tagged status=success and codespace`() {
        val registry = SimpleMeterRegistry()
        val metrics = FilterMetrics(registry)

        metrics.incrementSuccessfulRun("RUT")

        assertEquals(1.0, counterFor(registry, "success", "RUT"))
        assertEquals(0.0, counterFor(registry, "failed", "RUT"))
    }

    @Test
    fun `incrementFailedRun records one run tagged status=failed and codespace`() {
        val registry = SimpleMeterRegistry()
        val metrics = FilterMetrics(registry)

        metrics.incrementFailedRun("RUT")

        assertEquals(1.0, counterFor(registry, "failed", "RUT"))
        assertEquals(0.0, counterFor(registry, "success", "RUT"))
    }

    @Test
    fun `runs are counted separately per codespace`() {
        val registry = SimpleMeterRegistry()
        val metrics = FilterMetrics(registry)

        metrics.incrementSuccessfulRun("RUT")
        metrics.incrementSuccessfulRun("RUT")
        metrics.incrementSuccessfulRun("ATB")

        assertEquals(2.0, counterFor(registry, "success", "RUT"))
        assertEquals(1.0, counterFor(registry, "success", "ATB"))
    }

    @Test
    fun `null or blank codespace is recorded as unknown`() {
        val registry = SimpleMeterRegistry()
        val metrics = FilterMetrics(registry)

        metrics.incrementSuccessfulRun(null)
        metrics.incrementFailedRun("  ")

        assertEquals(1.0, counterFor(registry, "success", "unknown"))
        assertEquals(1.0, counterFor(registry, "failed", "unknown"))
    }

    @Test
    fun `recordSuccessfulRunDuration records the duration tagged by codespace`() {
        val registry = SimpleMeterRegistry()
        val metrics = FilterMetrics(registry)

        metrics.recordSuccessfulRunDuration("RUT", Duration.ofMillis(1500))

        assertEquals(1L, durationCount(registry, "RUT"))
        assertEquals(1500.0, durationTotalMillis(registry, "RUT"), 1.0)
    }

    @Test
    fun `durations are recorded and accumulated separately per codespace`() {
        val registry = SimpleMeterRegistry()
        val metrics = FilterMetrics(registry)

        metrics.recordSuccessfulRunDuration("RUT", Duration.ofMillis(1000))
        metrics.recordSuccessfulRunDuration("RUT", Duration.ofMillis(2000))
        metrics.recordSuccessfulRunDuration("ATB", Duration.ofMillis(500))

        assertEquals(2L, durationCount(registry, "RUT"))
        assertEquals(3000.0, durationTotalMillis(registry, "RUT"), 1.0)
        assertEquals(1L, durationCount(registry, "ATB"))
        assertEquals(500.0, durationTotalMillis(registry, "ATB"), 1.0)
    }

    @Test
    fun `null or blank codespace duration is recorded as unknown`() {
        val registry = SimpleMeterRegistry()
        val metrics = FilterMetrics(registry)

        metrics.recordSuccessfulRunDuration(null, Duration.ofMillis(100))

        assertEquals(1L, durationCount(registry, "unknown"))
    }

    @Test
    fun `setServiceJourneysKept registers a gauge with the kept count tagged by codespace`() {
        val registry = SimpleMeterRegistry()
        val metrics = FilterMetrics(registry)

        metrics.setServiceJourneysKept("RUT", keptCount = 42, originalCount = 100)

        assertEquals(42.0, serviceJourneysKept(registry, "RUT"))
    }

    @Test
    fun `setServiceJourneysKept replaces the value on a later run and does not accumulate`() {
        val registry = SimpleMeterRegistry()
        val metrics = FilterMetrics(registry)

        metrics.setServiceJourneysKept("RUT", keptCount = 42, originalCount = 100)
        metrics.setServiceJourneysKept("RUT", keptCount = 40, originalCount = 95)

        assertEquals(40.0, serviceJourneysKept(registry, "RUT"))
    }

    @Test
    fun `service journeys kept are tracked separately per codespace`() {
        val registry = SimpleMeterRegistry()
        val metrics = FilterMetrics(registry)

        metrics.setServiceJourneysKept("RUT", keptCount = 42, originalCount = 100)
        metrics.setServiceJourneysKept("ATB", keptCount = 7, originalCount = 10)

        assertEquals(42.0, serviceJourneysKept(registry, "RUT"))
        assertEquals(7.0, serviceJourneysKept(registry, "ATB"))
    }

    @Test
    fun `null or blank codespace kept count is recorded as unknown`() {
        val registry = SimpleMeterRegistry()
        val metrics = FilterMetrics(registry)

        metrics.setServiceJourneysKept(null, keptCount = 5, originalCount = 10)

        assertEquals(5.0, serviceJourneysKept(registry, "unknown"))
    }

    @Test
    fun `setServiceJourneysKept records the run's update time in millis tagged by codespace`() {
        val registry = SimpleMeterRegistry()
        val metrics = FilterMetrics(registry)

        metrics.setServiceJourneysKept("RUT", keptCount = 42, originalCount = 100, epochMillis = 1_761_000_000_123L)

        assertEquals(1_761_000_000_123.0, serviceJourneysKeptUpdatedMs(registry, "RUT"))
    }

    @Test
    fun `setServiceJourneysKept advances the update time on each run`() {
        val registry = SimpleMeterRegistry()
        val metrics = FilterMetrics(registry)

        metrics.setServiceJourneysKept("RUT", keptCount = 42, originalCount = 100, epochMillis = 1000L)
        metrics.setServiceJourneysKept("RUT", keptCount = 40, originalCount = 95, epochMillis = 2000L)

        assertEquals(2000.0, serviceJourneysKeptUpdatedMs(registry, "RUT"))
    }

    @Test
    fun `setServiceJourneysKept registers a gauge for the original count tagged by codespace`() {
        val registry = SimpleMeterRegistry()
        val metrics = FilterMetrics(registry)

        metrics.setServiceJourneysKept("RUT", keptCount = 42, originalCount = 100)

        assertEquals(100.0, serviceJourneysOriginal(registry, "RUT"))
    }

    @Test
    fun `setServiceJourneysKept replaces the original count on a later run`() {
        val registry = SimpleMeterRegistry()
        val metrics = FilterMetrics(registry)

        metrics.setServiceJourneysKept("RUT", keptCount = 42, originalCount = 100)
        metrics.setServiceJourneysKept("RUT", keptCount = 40, originalCount = 95)

        assertEquals(95.0, serviceJourneysOriginal(registry, "RUT"))
    }

    @Test
    fun `original counts are tracked separately per codespace`() {
        val registry = SimpleMeterRegistry()
        val metrics = FilterMetrics(registry)

        metrics.setServiceJourneysKept("RUT", keptCount = 42, originalCount = 100)
        metrics.setServiceJourneysKept("ATB", keptCount = 7, originalCount = 10)

        assertEquals(100.0, serviceJourneysOriginal(registry, "RUT"))
        assertEquals(10.0, serviceJourneysOriginal(registry, "ATB"))
    }

    @Test
    fun `null or blank codespace original count is recorded as unknown`() {
        val registry = SimpleMeterRegistry()
        val metrics = FilterMetrics(registry)

        metrics.setServiceJourneysKept(null, keptCount = 5, originalCount = 10)

        assertEquals(10.0, serviceJourneysOriginal(registry, "unknown"))
    }

    private fun guardCount(
        registry: SimpleMeterRegistry,
        outcome: String,
        codespace: String,
    ): Double =
        registry.find(FilterMetrics.GUARD_METRIC_NAME)
            .tags("outcome", outcome, "codespace", codespace)
            .counter()
            ?.count() ?: 0.0

    @Test
    fun `recordGuardOutcome counts per outcome and codespace`() {
        val registry = SimpleMeterRegistry()
        val metrics = FilterMetrics(registry)

        metrics.recordGuardOutcome(FilterMetrics.GUARD_OUTCOME_BOUNCED_FRESH, "RUT")
        metrics.recordGuardOutcome(FilterMetrics.GUARD_OUTCOME_BOUNCED_FRESH, "RUT")
        metrics.recordGuardOutcome(FilterMetrics.GUARD_OUTCOME_SKIPPED_DONE, "RUT")

        assertEquals(2.0, guardCount(registry, FilterMetrics.GUARD_OUTCOME_BOUNCED_FRESH, "RUT"))
        assertEquals(1.0, guardCount(registry, FilterMetrics.GUARD_OUTCOME_SKIPPED_DONE, "RUT"))
    }

    @Test
    fun `recordGuardOutcome records null codespace as unknown`() {
        val registry = SimpleMeterRegistry()
        val metrics = FilterMetrics(registry)

        metrics.recordGuardOutcome(FilterMetrics.GUARD_OUTCOME_FAIL_OPEN, null)

        assertEquals(1.0, guardCount(registry, FilterMetrics.GUARD_OUTCOME_FAIL_OPEN, "unknown"))
    }
}
