package org.entur.ror.ashur.pubsub

import com.google.pubsub.v1.PubsubMessage
import org.entur.ror.ashur.FilterService
import org.entur.ror.ashur.getCorrelationId
import org.entur.ror.ashur.getNetexFileName
import org.entur.ror.ashur.getCodespace
import org.entur.ror.ashur.setupFileService
import org.slf4j.LoggerFactory
import java.util.Properties

class NetexFilterMessageHandler(config: Properties): MessageHandler {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val inputDirectory = config.getProperty("input.path")
    private val outputDirectory = config.getProperty("output.path")

    private val filterService = FilterService(
        fileService = setupFileService(config),
        cleanUpEnabled = config.getProperty("cleanup.enabled").toBoolean(),
    )

    fun getPathOfInputDirectoryForMessage(message: PubsubMessage): String =
        "${inputDirectory}/${message.getCodespace()}/${message.getCorrelationId()}"

    fun getPathOfOutputDirectoryForMessage(message: PubsubMessage): String =
        "${outputDirectory}/${message.getCodespace()}/${message.getCorrelationId()}"

    override fun handleMessage(message: PubsubMessage) {
        try {
            val fileName: String? = message.getNetexFileName()
            filterService.handleFilterRequestForFile(
                fileName,
                inputDirectory = getPathOfInputDirectoryForMessage(message),
                outputDirectory = getPathOfOutputDirectoryForMessage(message),
            )
        } catch (e: Exception) {
            logger.error("Exception occurred while processing message", e)
            throw e
        }
    }
}
