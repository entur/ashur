package org.entur.ror.ashur.camel

import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.entur.ror.ashur.Constants
import org.entur.ror.ashur.pubsub.NetexFilterMessageHandler
import org.entur.ror.ashur.toPubsubMessage
import org.springframework.stereotype.Component

/**
 * NetexFilterMessageProcessor is a Camel processor that handles messages for filtering Netex.
 * It uses the NetexFilterMessageHandler to process the incoming Pub/Sub messages.
 *
 * @param config Properties containing configuration settings such as input and output directories.
 */
@Component
class NetexFilterMessageProcessor(
    private val messageHandler: NetexFilterMessageHandler
): Processor {
    override fun process(exchange: Exchange) {
        val pubsubMessage = exchange.toPubsubMessage()
        val pathToFilteredZipFile = messageHandler.handleMessage(pubsubMessage)
        exchange.getIn().setHeader(Constants.FILTERED_NETEX_FILE_PATH_HEADER, pathToFilteredZipFile)
    }
}