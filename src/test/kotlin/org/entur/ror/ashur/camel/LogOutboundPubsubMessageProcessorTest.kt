package org.entur.ror.ashur.camel

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.support.DefaultExchange
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

class LogOutboundPubsubMessageProcessorTest {
    private lateinit var appender: ListAppender<ILoggingEvent>
    private lateinit var logger: Logger

    @BeforeEach
    fun setUp() {
        logger = LoggerFactory.getLogger(LogOutboundPubsubMessageProcessor::class.java) as Logger
        appender = ListAppender<ILoggingEvent>().also { it.start() }
        logger.addAppender(appender)
    }

    @AfterEach
    fun tearDown() {
        logger.detachAppender(appender)
    }

    @Test
    fun `logs outbound message with attributes and body`() {
        val processor = LogOutboundPubsubMessageProcessor()
        val exchange = DefaultExchange(DefaultCamelContext())
        exchange.getIn().body = """{"status":"SUCCESS"}"""
        exchange.getIn().setHeader(
            "CamelGooglePubsubAttributes",
            mapOf(
                "Status" to "SUCCESS",
                "EnturDatasetReferential" to "abc",
                "FilteredNetexFilePath" to "abc/corr-1/filtered_file.zip",
            )
        )

        processor.process(exchange)

        assertEquals(1, appender.list.size)
        val event = appender.list[0]
        assertEquals(Level.INFO, event.level)
        assertEquals("Publishing Pub/Sub message", event.message)

        val fields = event.marker!!.fieldMap()
        assertEquals("outbound", fields["direction"])
        assertEquals("""{"status":"SUCCESS"}""", fields["pubsubBody"])
        val attrs = fields["pubsubAttributes"] as Map<*, *>
        assertEquals("SUCCESS", attrs["Status"])
        assertEquals("abc", attrs["EnturDatasetReferential"])
        assertEquals("abc/corr-1/filtered_file.zip", attrs["FilteredNetexFilePath"])
    }

    @Test
    fun `omits body field when body is empty`() {
        val processor = LogOutboundPubsubMessageProcessor()
        val exchange = DefaultExchange(DefaultCamelContext())
        exchange.getIn().body = ""
        exchange.getIn().setHeader(
            "CamelGooglePubsubAttributes",
            mapOf("Status" to "STARTED")
        )

        processor.process(exchange)

        assertEquals(1, appender.list.size)
        val event = appender.list[0]
        val fields = event.marker!!.fieldMap()
        assertNotNull(fields["pubsubAttributes"])
        assertFalse(fields.containsKey("pubsubBody"))
    }
}
