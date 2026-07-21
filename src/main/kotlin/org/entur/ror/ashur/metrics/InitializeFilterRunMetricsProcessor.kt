package org.entur.ror.ashur.metrics

import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.slf4j.LoggerFactory

/**
 * Camel processor that pre-registers the run counters for the exchange's codespace at run start.
 *
 * Reads the `codespace` routing header (set at route entry) and asks [FilterMetrics] to register the
 * success and failed counters at zero, so a Prometheus scrape observes the `0` baseline before the
 * first run finishes. Without this, `rate()`/`increase()` cannot see the first `0 -> 1` increment and
 * drop the first run of each (codespace, pod) series.
 *
 * @param filterMetrics the metrics component to initialise the counters on.
 */
class InitializeFilterRunMetricsProcessor(
    private val filterMetrics: FilterMetrics,
) : Processor {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun process(exchange: Exchange) {
        val codespace = exchange.getIn().getHeader(CODESPACE_HEADER, String::class.java)
        // Never let a metrics side-effect fail the filter run: this runs on the main route before
        // status STARTED, so a thrown exception would be handled as a FAILED run.
        try {
            filterMetrics.initializeRunCounters(codespace)
            logger.debug("Initialised filter-run metric counters at 0 for codespace: {}", codespace ?: "unknown")
        } catch (e: Exception) {
            logger.warn(
                "Failed to initialise filter-run metric counters for codespace: {}; continuing the run",
                codespace ?: "unknown",
                e,
            )
        }
    }

    companion object {
        const val CODESPACE_HEADER = "codespace"
    }
}
