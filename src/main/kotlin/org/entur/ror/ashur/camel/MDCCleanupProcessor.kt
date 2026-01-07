package org.entur.ror.ashur.camel

import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.slf4j.MDC

/**
 * MDCCleanupProcessor is a Camel processor that cleans up the MDC (Mapped Diagnostic Context)
 * by removing the correlation ID and codespace after processing is complete.
 *
 * This is typically used in the onCompletion phase of a Camel route to ensure that
 * the MDC context does not retain information from previous messages, preventing potential
 * information leakage or confusion in subsequent log entries.
 */
class MDCCleanupProcessor : Processor {
    override fun process(exchange: Exchange) {
        MDC.remove("correlationId")
        MDC.remove("codespace")
    }
}