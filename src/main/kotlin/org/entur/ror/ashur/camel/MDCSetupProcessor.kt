package org.entur.ror.ashur.camel

import org.entur.ror.ashur.getPubsubAttributes
import org.slf4j.MDC

/**
 * MDCSetupProcessor is a Camel processor that sets up the MDC (Mapped Diagnostic Context)
 * with correlation ID and codespace from the Pub/Sub attributes of the exchange, such that
 * they can be used for logging purposes.
 */
class MDCSetupProcessor : org.apache.camel.Processor {
    override fun process(exchange: org.apache.camel.Exchange) {
        val pubsubAttributes = exchange.getPubsubAttributes()
        MDC.put("correlationId", pubsubAttributes.get("CorrelationId") ?: "unknown")
        MDC.put("codespace", pubsubAttributes.get("Codespace") ?: "unknown")
    }
}