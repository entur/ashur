package org.entur.ror.ashur.camel

import org.apache.camel.Exchange
import org.apache.camel.Processor

class SetFilteringStatusProcessor(val status: String): Processor {
    override fun process(exchange: Exchange) {
        val existingAttributes = exchange
            .getIn()
            .getHeader("CamelGooglePubsubAttributes", Map::class.java)
            .toMutableMap()
        existingAttributes["Status"] = status
        exchange.getIn().setHeader("CamelGooglePubsubAttributes", existingAttributes)
    }
}