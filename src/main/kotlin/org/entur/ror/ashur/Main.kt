package org.entur.ror.ashur

import org.entur.ror.ashur.pubsub.NetexFilterMessageHandler

fun main() {
    val config = getConfiguration()
    val messageHandler = NetexFilterMessageHandler(
        inputDirectory = config.getProperty("input.path"),
        outputDirectory = config.getProperty("output.path"),
        config = config
    )
    val pubsubListener = setupPubsubListener(
        messageHandler = messageHandler,
        config = config
    )
    pubsubListener.listen()
}
