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
 * - [SERVICE_JOURNEYS_KEPT_METRIC_NAME] (`ashur_service_journeys_kept`) is a gauge tagged with
 *   `codespace`, set on each successful run to how many `ServiceJourney` entities survived filtering.
 *   The gauge only holds the latest run's number (each run replaces it in memory); the history over
 *   time is kept by Prometheus, which records a sample every scrape — it is not stored in the app.
 *   After a restart the app has no value until the next run, so the graph shows a gap rather than a
 *   drop to zero (the samples Prometheus already recorded are untouched).
 * - [SERVICE_JOURNEYS_KEPT_UPDATED_MS_METRIC_NAME] (`ashur_service_journeys_kept_updated_ms`) is a
 *   companion gauge holding the time (in ms) of that run. It exists only so a dashboard can tell,
 *   when Ashur runs on more than one pod, which pod's count is the newer one — see [setServiceJourneysKept].
 *
 * Because `codespace` is a label, a Grafana dashboard can both group by codespace
 * (`sum by (codespace) (...)`) and aggregate across codespaces (`sum without (codespace) (...)`).
 */
@Component
class FilterMetrics(private val meterRegistry: MeterRegistry) {

    // A gauge only keeps a weak reference to the value behind it, so we hold that value ourselves
    // (one number per codespace, per gauge) and overwrite it on each run.
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
     * Records, for [codespace], how many `ServiceJourney` entities survived filtering on this run
     * ([count]), together with when the run happened ([epochMillis], defaulting to now).
     *
     * The time is recorded so a dashboard can draw a correct history over time. Ashur runs on more
     * than one pod, and each pod only remembers the counts for the runs it handled itself, so no
     * single pod has the whole picture for a codespace. Knowing when each pod's count was last set
     * lets a dashboard always use the newer one, and so show the count rising and falling exactly as
     * it really did. Without the time it could only guess (say, show the biggest), which would hide
     * the runs where the count dropped.
     *
     * Both gauges are created the first time a codespace appears, then just updated in place on later runs.
     */
    fun setServiceJourneysKept(codespace: String?, count: Int, epochMillis: Long = System.currentTimeMillis()) {
        val resolvedCodespace = codespace.orUnknown()
        gaugeHolder(serviceJourneysKeptValues, SERVICE_JOURNEYS_KEPT_METRIC_NAME, resolvedCodespace).set(count.toLong())
        gaugeHolder(serviceJourneysKeptUpdatedMs, SERVICE_JOURNEYS_KEPT_UPDATED_MS_METRIC_NAME, resolvedCodespace)
            .set(epochMillis)
    }

    /**
     * Returns the value behind the [metricName] gauge for [codespace], creating and registering the
     * gauge the first time we see this codespace and reusing it afterwards.
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
