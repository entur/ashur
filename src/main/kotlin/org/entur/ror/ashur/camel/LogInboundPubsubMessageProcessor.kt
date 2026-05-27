package org.entur.ror.ashur.camel

import net.logstash.logback.marker.Markers
import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.slf4j.LoggerFactory

/**
 * Logs every Pub/Sub message Ashur consumes. The full attribute map is emitted as a structured
 * `pubsubAttributes` field so the audit log is queryable in log aggregation.
 */
class LogInboundPubsubMessageProcessor : Processor {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun process(exchange: Exchange) {
        val rawAttributes = exchange.getIn().getHeader("CamelGooglePubsubAttributes", Map::class.java)
        val attributes = rawAttributes?.entries?.associate { (k, v) -> k.toString() to v?.toString() }
            ?: emptyMap()
        logger.info(
            Markers.appendEntries(
                mapOf(
                    "direction" to "inbound",
                    "pubsubAttributes" to attributes,
                )
            ),
            "Received Pub/Sub message"
        )
    }
}
