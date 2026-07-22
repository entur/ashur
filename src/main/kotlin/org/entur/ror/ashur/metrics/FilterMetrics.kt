package org.entur.ror.ashur.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.stereotype.Component
import java.time.Duration

/**
 * Emits application metrics for filtering runs.
 *
 * - [RUNS_METRIC_NAME] is a counter tagged with `status` (`success`/`failed`) and `codespace`.
 *   In Prometheus this surfaces as `ashur_filter_runs_total`.
 * - [DURATION_METRIC_NAME] is a timer tagged with `codespace`, recorded only for successful runs.
 *   In Prometheus this surfaces as `ashur_filter_duration_seconds_count` / `_sum`, so the average
 *   run time is `rate(..._sum[5m]) / rate(..._count[5m])`.
 *
 * Because `codespace` is a label, a Grafana dashboard can both group by codespace
 * (`sum by (codespace) (...)`) and aggregate across codespaces (`sum without (codespace) (...)`).
 */
@Component
class FilterMetrics(private val meterRegistry: MeterRegistry) {

    fun incrementSuccessfulRun(codespace: String?) = increment(STATUS_SUCCESS, codespace)

    fun incrementFailedRun(codespace: String?) = increment(STATUS_FAILED, codespace)

    fun recordSuccessfulRunDuration(codespace: String?, duration: Duration) {
        Timer.builder(DURATION_METRIC_NAME)
            .tag("codespace", codespace.orUnknown())
            .register(meterRegistry)
            .record(duration)
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
        const val STATUS_SUCCESS = "success"
        const val STATUS_FAILED = "failed"
        const val UNKNOWN_CODESPACE = "unknown"
    }
}
