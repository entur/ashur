package org.entur.ror.ashur.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Emits application metrics for filtering runs.
 *
 * - [RUNS_METRIC_NAME] is a counter tagged with `status` (`success`/`failed`) and `codespace`.
 *   In Prometheus this surfaces as `ashur_filter_runs_total`.
 * - [DURATION_METRIC_NAME] is a timer tagged with `codespace`, recorded only for successful runs.
 *   In Prometheus this surfaces as `ashur_filter_duration_seconds_count` / `_sum`, so the average
 *   run time is `rate(..._sum[5m]) / rate(..._count[5m])`.
 * - [SERVICE_JOURNEYS_KEPT_METRIC_NAME] is a gauge tagged with `codespace`, set on each successful
 *   run to the number of `ServiceJourney` entities that survived filtering. In Prometheus this
 *   surfaces as `ashur_service_journeys_kept`. It is a level (re-measured each run), not an
 *   accumulating count, so a pod restart leaves a gap rather than resetting to zero.
 * - [SERVICE_JOURNEYS_KEPT_UPDATED_MS_METRIC_NAME] is a companion gauge tagged with `codespace`, set
 *   alongside it to the run's wall-clock time in milliseconds (`ashur_service_journeys_kept_updated_ms`).
 *   It is the recency signal that lets a dashboard pick the most-recently-updated pod per codespace
 *   across replicas, so a per-codespace history stays correct even when the kept count decreases.
 *
 * Because `codespace` is a label, a Grafana dashboard can both group by codespace
 * (`sum by (codespace) (...)`) and aggregate across codespaces (`sum without (codespace) (...)`).
 */
@Component
class FilterMetrics(private val meterRegistry: MeterRegistry) {

    // Micrometer holds a weak reference to a gauge's backing state, so we keep a strong reference
    // to one AtomicLong per codespace (per gauge) and update it in place on each run.
    private val serviceJourneysKeptValues = ConcurrentHashMap<String, AtomicLong>()
    private val serviceJourneysKeptUpdatedMs = ConcurrentHashMap<String, AtomicLong>()

    fun incrementSuccessfulRun(codespace: String?) = increment(STATUS_SUCCESS, codespace)

    fun incrementFailedRun(codespace: String?) = increment(STATUS_FAILED, codespace)

    fun recordSuccessfulRunDuration(codespace: String?, duration: Duration) {
        Timer.builder(DURATION_METRIC_NAME)
            .tag("codespace", codespace.orUnknown())
            .register(meterRegistry)
            .record(duration)
    }

    /**
     * Records, for [codespace], the number of `ServiceJourney` entities that survived filtering on
     * the current run ([count]) as the [SERVICE_JOURNEYS_KEPT_METRIC_NAME] gauge, and the wall-clock
     * time of the run ([epochMillis], defaulting to now) as the
     * [SERVICE_JOURNEYS_KEPT_UPDATED_MS_METRIC_NAME] gauge.
     *
     * The timestamp is the recency signal a dashboard needs to reconstruct a correct per-codespace
     * history across replicas: each pod holds its own last-run value per codespace, so a query picks
     * the value from whichever pod ran the codespace most recently (largest timestamp) rather than
     * the largest value. Both gauges are registered once per codespace on first sight; later runs
     * update the backing value in place.
     */
    fun setServiceJourneysKept(codespace: String?, count: Int, epochMillis: Long = System.currentTimeMillis()) {
        val resolvedCodespace = codespace.orUnknown()
        gaugeHolder(serviceJourneysKeptValues, SERVICE_JOURNEYS_KEPT_METRIC_NAME, resolvedCodespace).set(count.toLong())
        gaugeHolder(serviceJourneysKeptUpdatedMs, SERVICE_JOURNEYS_KEPT_UPDATED_MS_METRIC_NAME, resolvedCodespace)
            .set(epochMillis)
    }

    /**
     * Returns the strongly-held [AtomicLong] backing the [metricName] gauge for [codespace],
     * registering the gauge on first sight. Registration is idempotent per codespace.
     */
    private fun gaugeHolder(
        holders: ConcurrentHashMap<String, AtomicLong>,
        metricName: String,
        codespace: String,
    ): AtomicLong =
        holders.computeIfAbsent(codespace) { resolvedCodespace ->
            AtomicLong().also { value ->
                Gauge.builder(metricName, value) { it.get().toDouble() }
                    .tag("codespace", resolvedCodespace)
                    .register(meterRegistry)
            }
        }

    private fun increment(status: String, codespace: String?) {
        Counter.builder(RUNS_METRIC_NAME)
            .tag("status", status)
            .tag("codespace", codespace.orUnknown())
            .register(meterRegistry)
            .increment()
    }

    private fun String?.orUnknown(): String = this?.takeIf { it.isNotBlank() } ?: UNKNOWN_CODESPACE

    companion object {
        const val RUNS_METRIC_NAME = "ashur.filter.runs"
        const val DURATION_METRIC_NAME = "ashur.filter.duration"
        const val SERVICE_JOURNEYS_KEPT_METRIC_NAME = "ashur.service.journeys.kept"
        const val SERVICE_JOURNEYS_KEPT_UPDATED_MS_METRIC_NAME = "ashur.service.journeys.kept.updated.ms"
        const val STATUS_SUCCESS = "success"
        const val STATUS_FAILED = "failed"
        const val UNKNOWN_CODESPACE = "unknown"
    }
}
