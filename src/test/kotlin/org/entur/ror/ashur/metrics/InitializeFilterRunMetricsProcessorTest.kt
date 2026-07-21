package org.entur.ror.ashur.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.support.DefaultExchange
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class InitializeFilterRunMetricsProcessorTest {
    private fun counter(registry: SimpleMeterRegistry, status: String, codespace: String): Counter? =
        registry.find(FilterMetrics.RUNS_METRIC_NAME)
            .tags("status", status, "codespace", codespace)
            .counter()

    @Test
    fun `registers success and failed counters at zero for the codespace on the exchange`() {
        val registry = SimpleMeterRegistry()
        val metrics = FilterMetrics(registry)
        val exchange = DefaultExchange(DefaultCamelContext())
        exchange.getIn().setHeader("codespace", "RUT")

        InitializeFilterRunMetricsProcessor(metrics).process(exchange)

        assertNotNull(counter(registry, "success", "RUT"))
        assertNotNull(counter(registry, "failed", "RUT"))
        assertEquals(0.0, counter(registry, "success", "RUT")?.count())
        assertEquals(0.0, counter(registry, "failed", "RUT")?.count())
    }

    @Test
    fun `registers counters under unknown when the codespace header is missing`() {
        val registry = SimpleMeterRegistry()
        val metrics = FilterMetrics(registry)
        val exchange = DefaultExchange(DefaultCamelContext())

        InitializeFilterRunMetricsProcessor(metrics).process(exchange)

        assertNotNull(counter(registry, "success", "unknown"))
        assertNotNull(counter(registry, "failed", "unknown"))
    }

    @Test
    fun `does not fail the run when counter initialisation throws`() {
        val metrics = mock<FilterMetrics>()
        doThrow(RuntimeException("meter registry unavailable"))
            .whenever(metrics).initializeRunCounters(any())
        val exchange = DefaultExchange(DefaultCamelContext())
        exchange.getIn().setHeader("codespace", "RUT")

        assertDoesNotThrow {
            InitializeFilterRunMetricsProcessor(metrics).process(exchange)
        }
    }
}
