package org.entur.ror.ashur.pubsub

import com.google.pubsub.v1.PubsubMessage
import org.entur.netex.tools.pipeline.config.CliConfig
import org.entur.ror.ashur.filter.FilterService
import org.entur.ror.ashur.config.AppConfig
import org.entur.ror.ashur.filter.FilterConfigLoader
import org.entur.ror.ashur.getCorrelationId
import org.entur.ror.ashur.getNetexFileName
import org.entur.ror.ashur.getCodespace
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Handles incoming messages from a Pub/Sub topic and processes Netex files based on a filter configuration.
 *
 * @param config The configuration properties for the message handler.
 */
@Component
class NetexFilterMessageHandler(
    private val appConfig: AppConfig,
    private val filterService: FilterService,
): MessageHandler {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val filterConfigLoader = FilterConfigLoader()

    fun getPathOfInputDirectoryForMessage(message: PubsubMessage): String {
        return "${appConfig.netex.inputPath}/${message.getCodespace()}/${message.getCorrelationId()}"
    }

    fun getPathOfOutputDirectoryForMessage(message: PubsubMessage): String {
        return "${appConfig.netex.outputPath}/${message.getCodespace()}/${message.getCorrelationId()}"
    }

    fun getFilterConfig(): CliConfig? = filterConfigLoader.loadFilterConfig()

    /**
     * Performs the filtering operation on the Netex file specified in the Pub/Sub message.
     **/
    override fun handleMessage(message: PubsubMessage) {
        try {
            val fileName: String? = message.getNetexFileName()
            val filterConfig: CliConfig = getFilterConfig()!!
            filterService.handleFilterRequestForFile(
                fileName,
                inputDirectory = getPathOfInputDirectoryForMessage(message),
                outputDirectory = getPathOfOutputDirectoryForMessage(message),
                filterConfig = filterConfig
            )
        } catch (e: Exception) {
            logger.error("Exception occurred while processing message", e)
            throw e
        }
    }
}
