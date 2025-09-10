package org.entur.ror.ashur.camel

import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.support.DefaultExchange
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.slf4j.MDC

class MDCSetupProcessorTest {
    private val testCorrelationId = "test-correlation-id"
    private val testCodespace = "test-codespace"

    @Test
    fun testMdcSetupProcessor() {
        val processor = MDCSetupProcessor()
        val exchange = DefaultExchange(DefaultCamelContext())

        exchange.getIn().setBody("")
        exchange.getIn().setHeader("CamelGooglePubsubAttributes", mapOf(
            "RutebankenCorrelationId" to testCorrelationId,
            "EnturDatasetReferential" to testCodespace
        ))

        processor.process(exchange)
        assertEquals(testCorrelationId, MDC.get("correlationId"))
        assertEquals(testCodespace, MDC.get("codespace"))
    }
}