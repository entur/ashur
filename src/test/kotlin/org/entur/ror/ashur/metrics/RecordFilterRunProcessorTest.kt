package org.entur.ror.ashur.metrics

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.support.DefaultExchange
import kotlin.test.Test
import kotlin.test.assertEquals

class RecordFilterRunProcessorTest {
    private fun count(registry: SimpleMeterRegistry, status: String, codespace: String): Double =
        registry.find(FilterMetrics.RUNS_METRIC_NAME)
            .tags("status", status, "codespace", codespace)
            .counter()
            ?.count() ?: 0.0

    @Test
    fun `records a successful run for the codespace on the exchange`() {
        val registry = SimpleMeterRegistry()
        val metrics = FilterMetrics(registry)
        val exchange = DefaultExchange(DefaultCamelContext())
        exchange.getIn().setHeader("codespace", "RUT")

        RecordFilterRunProcessor(metrics, successful = true).process(exchange)

        assertEquals(1.0, count(registry, "success", "RUT"))
    }

    @Test
    fun `records a failed run for the codespace on the exchange`() {
        val registry = SimpleMeterRegistry()
        val metrics = FilterMetrics(registry)
        val exchange = DefaultExchange(DefaultCamelContext())
        exchange.getIn().setHeader("codespace", "ATB")

        RecordFilterRunProcessor(metrics, successful = false).process(exchange)

        assertEquals(1.0, count(registry, "failed", "ATB"))
    }

    @Test
    fun `records unknown when the codespace header is missing`() {
        val registry = SimpleMeterRegistry()
        val metrics = FilterMetrics(registry)
        val exchange = DefaultExchange(DefaultCamelContext())

        RecordFilterRunProcessor(metrics, successful = true).process(exchange)

        assertEquals(1.0, count(registry, "success", "unknown"))
    }
}
