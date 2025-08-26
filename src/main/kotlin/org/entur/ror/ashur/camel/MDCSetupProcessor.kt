package org.entur.ror.ashur.camel

import org.entur.ror.ashur.getCodespace
import org.entur.ror.ashur.getCorrelationId
import org.entur.ror.ashur.toPubsubMessage
import org.slf4j.MDC

/**
 * MDCSetupProcessor is a Camel processor that sets up the MDC (Mapped Diagnostic Context)
 * with correlation ID and codespace from the Pub/Sub attributes of the exchange, such that
 * they can be used for logging purposes.
 */
class MDCSetupProcessor : org.apache.camel.Processor {
    override fun process(exchange: org.apache.camel.Exchange) {
        val pubsubMessage = exchange.toPubsubMessage()
        MDC.put("correlationId", pubsubMessage.getCorrelationId() ?: "unknown")
        MDC.put("codespace", pubsubMessage.getCodespace() ?: "unknown")
    }
}