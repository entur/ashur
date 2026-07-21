package org.entur.ror.ashur.metrics

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

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
    fun `initializeRunCounters registers success and failed counters at zero`() {
        val registry = SimpleMeterRegistry()
        val metrics = FilterMetrics(registry)

        metrics.initializeRunCounters("RUT")

        assertNotNull(
            registry.find(FilterMetrics.RUNS_METRIC_NAME)
                .tags("status", "success", "codespace", "RUT").counter(),
            "success counter must be registered so a scrape sees the 0 baseline",
        )
        assertNotNull(
            registry.find(FilterMetrics.RUNS_METRIC_NAME)
                .tags("status", "failed", "codespace", "RUT").counter(),
            "failed counter must be registered so a scrape sees the 0 baseline",
        )
        assertEquals(0.0, counterFor(registry, "success", "RUT"))
        assertEquals(0.0, counterFor(registry, "failed", "RUT"))
    }

    @Test
    fun `initializeRunCounters does not reset an existing count`() {
        val registry = SimpleMeterRegistry()
        val metrics = FilterMetrics(registry)

        metrics.incrementSuccessfulRun("RUT")
        metrics.initializeRunCounters("RUT")

        assertEquals(1.0, counterFor(registry, "success", "RUT"))
        assertEquals(0.0, counterFor(registry, "failed", "RUT"))
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
}
