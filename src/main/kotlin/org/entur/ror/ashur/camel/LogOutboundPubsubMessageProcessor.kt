package org.entur.ror.ashur.camel

import net.logstash.logback.marker.Markers
import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.slf4j.LoggerFactory

/**
 * Logs every Pub/Sub message Ashur publishes. Both the attribute map and the serialized body
 * are emitted as structured fields so a full audit trail is queryable in log aggregation. The
 * body is included only when present (STARTED publishes have no body).
 */
class LogOutboundPubsubMessageProcessor : Processor {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun process(exchange: Exchange) {
        val rawAttributes = exchange.getIn().getHeader("CamelGooglePubsubAttributes", Map::class.java)
        val attributes = rawAttributes?.entries?.associate { (k, v) -> k.toString() to v?.toString() }
            ?: emptyMap()
        val body = exchange.getIn().getBody(String::class.java)

        val fields = mutableMapOf<String, Any>(
            "direction" to "outbound",
            "pubsubAttributes" to attributes,
        )
        if (!body.isNullOrEmpty()) {
            fields["pubsubBody"] = body
        }

        logger.info(Markers.appendEntries(fields), "Publishing Pub/Sub message")
    }
}
