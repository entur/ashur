package org.entur.ror.ashur.camel

import org.apache.camel.AggregationStrategy
import org.apache.camel.Exchange
import org.slf4j.LoggerFactory

class LastMessageStrategy(
    private val subscriptionId: String,
): AggregationStrategy {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun aggregate(
        oldExchange: Exchange?,
        newExchange: Exchange
    ): Exchange? {
        val codespace = newExchange.getIn().getHeader("codespace")
        val correlationId = newExchange.getIn().getHeader("correlationId")

        if (oldExchange == null) {
            logger.info("Received message from Pub/Sub topic $subscriptionId. Starting aggregation for codespace $codespace...")
        }
        logger.info("Adding message with correlationId $correlationId to aggregation for group $codespace")

        return newExchange
    }
}