package org.entur.ror.ashur.camel

import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.support.DefaultExchange
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.slf4j.MDC

class MDCCleanupProcessorTest {
    @Test
    fun testMdcCleanupProcessor() {
        val exchange = DefaultExchange(DefaultCamelContext())
        val processor = MDCCleanupProcessor()

        MDC.put("correlationId", "test-correlation-id")
        MDC.put("codespace", "test-codespace")

        processor.process(exchange)

        assertNull(MDC.get("correlationId"))
        assertNull(MDC.get("codespace"))
    }
}