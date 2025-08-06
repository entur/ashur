package org.entur.ror.ashur.pubsub

import com.google.cloud.pubsub.v1.AckReplyConsumer
import com.google.pubsub.v1.PubsubMessage
import org.entur.ror.ashur.FilterService
import org.entur.ror.ashur.getCorrelationId
import org.entur.ror.ashur.getNetexFileName
import org.entur.ror.ashur.getCodespace
import org.entur.ror.ashur.setupFileService
import org.slf4j.LoggerFactory
import java.util.Properties

class NetexFilterMessageHandler(
    private val inputDirectory: String,
    private val outputDirectory: String,
    config: Properties,
): MessageHandler {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val filterService = FilterService(
        fileService = setupFileService(config),
        cleanUpEnabled = config.getProperty("cleanup.enabled").toBoolean(),
    )

    fun getPathOfInputDirectory(message: PubsubMessage): String =
        "${inputDirectory}/${message.getCodespace()}/${message.getCorrelationId()}"

    fun getPathOfOutputDirectory(message: PubsubMessage): String =
        "${outputDirectory}/${message.getCodespace()}/${message.getCorrelationId()}"

    override fun handleMessage(message: PubsubMessage, consumer: AckReplyConsumer) {
        try {
            val fileName: String? = message.getNetexFileName()
            filterService.handleFilterRequestForFile(
                fileName,
                inputDirectory = getPathOfInputDirectory(message),
                outputDirectory = getPathOfOutputDirectory(message),
            )
            consumer.ack()
        } catch (e: Exception) {
            logger.error("Exception occurred while processing message", e)
            consumer.nack()
            throw e
        }
    }
}
