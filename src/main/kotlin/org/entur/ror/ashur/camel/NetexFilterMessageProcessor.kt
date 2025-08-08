package org.entur.ror.ashur.camel

import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.entur.ror.ashur.pubsub.NetexFilterMessageHandler
import org.entur.ror.ashur.toPubsubMessage
import java.util.Properties

/**
 * NetexFilterMessageProcessor is a Camel processor that handles messages for filtering Netex.
 * It uses the NetexFilterMessageHandler to process the incoming Pub/Sub messages.
 *
 * @param config Properties containing configuration settings such as input and output directories.
 */
class NetexFilterMessageProcessor(
    private val config: Properties
): Processor {
    override fun process(exchange: Exchange) {
        val pubsubMessage = exchange.toPubsubMessage()
        val messageHandler = NetexFilterMessageHandler(config = config)
        messageHandler.handleMessage(pubsubMessage)
    }
}