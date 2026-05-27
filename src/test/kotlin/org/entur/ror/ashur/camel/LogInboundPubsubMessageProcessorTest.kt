package org.entur.ror.ashur.camel

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.support.DefaultExchange
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

class LogInboundPubsubMessageProcessorTest {
    private lateinit var appender: ListAppender<ILoggingEvent>
    private lateinit var logger: Logger

    @BeforeEach
    fun setUp() {
        logger = LoggerFactory.getLogger(LogInboundPubsubMessageProcessor::class.java) as Logger
        appender = ListAppender<ILoggingEvent>().also { it.start() }
        logger.addAppender(appender)
    }

    @AfterEach
    fun tearDown() {
        logger.detachAppender(appender)
    }

    @Test
    fun `logs inbound message at INFO with attributes`() {
        val processor = LogInboundPubsubMessageProcessor()
        val exchange = DefaultExchange(DefaultCamelContext())
        exchange.getIn().setBody("")
        exchange.getIn().setHeader(
            "CamelGooglePubsubAttributes",
            mapOf(
                "RutebankenCorrelationId" to "corr-1",
                "EnturDatasetReferential" to "abc",
                "EnturFilteringProfile" to "AsIsImportFilter",
            )
        )

        processor.process(exchange)

        assertEquals(1, appender.list.size)
        val event = appender.list[0]
        assertEquals(Level.INFO, event.level)
        assertEquals("Received Pub/Sub message", event.message)

        val fields = event.marker!!.fieldMap()
        assertEquals("inbound", fields["direction"])
        val attrs = fields["pubsubAttributes"] as Map<*, *>
        assertEquals("corr-1", attrs["RutebankenCorrelationId"])
        assertEquals("abc", attrs["EnturDatasetReferential"])
        assertEquals("AsIsImportFilter", attrs["EnturFilteringProfile"])
    }

    @Test
    fun `logs empty attributes map when header is missing`() {
        val processor = LogInboundPubsubMessageProcessor()
        val exchange = DefaultExchange(DefaultCamelContext())
        exchange.getIn().setBody("")

        processor.process(exchange)

        assertEquals(1, appender.list.size)
        val event = appender.list[0]
        val fields = event.marker!!.fieldMap()
        assertNotNull(fields["pubsubAttributes"])
        assertTrue((fields["pubsubAttributes"] as Map<*, *>).isEmpty())
    }
}
