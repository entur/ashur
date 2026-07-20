package org.entur.ror.ashur.metrics

import org.apache.camel.Exchange
import org.apache.camel.Processor

/**
 * Camel processor that records the outcome of a filtering run as a metric via [FilterMetrics].
 *
 * Reads the `codespace` routing header (set at route entry, and preserved on the exchange through
 * both the success and the handled-exception paths) so the counter is tagged per codespace.
 *
 * @param filterMetrics the metrics component to record the run on.
 * @param successful whether this processor sits on the success path (`true`) or the failure path (`false`).
 */
class RecordFilterRunProcessor(
    private val filterMetrics: FilterMetrics,
    private val successful: Boolean,
) : Processor {
    override fun process(exchange: Exchange) {
        val codespace = exchange.getIn().getHeader(CODESPACE_HEADER, String::class.java)
        if (successful) {
            filterMetrics.incrementSuccessfulRun(codespace)
        } else {
            filterMetrics.incrementFailedRun(codespace)
        }
    }

    companion object {
        private const val CODESPACE_HEADER = "codespace"
    }
}
