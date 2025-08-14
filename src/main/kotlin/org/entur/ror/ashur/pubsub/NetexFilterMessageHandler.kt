package org.entur.ror.ashur.pubsub

import com.google.pubsub.v1.PubsubMessage
import org.entur.ror.ashur.filter.FilterService
import org.entur.ror.ashur.config.AppConfig
import org.entur.ror.ashur.filter.FilterConfigResolver
import org.entur.ror.ashur.getCorrelationId
import org.entur.ror.ashur.getNetexFileName
import org.entur.ror.ashur.getCodespace
import org.entur.ror.ashur.getFilterProfile
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
    private val filterConfigResolver: FilterConfigResolver
): MessageHandler {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun getPathOfInputDirectoryForMessage(message: PubsubMessage): String {
        return "${appConfig.netex.inputPath}/${message.getCodespace()}/${message.getCorrelationId()}"
    }

    fun getPathOfOutputDirectoryForMessage(message: PubsubMessage): String {
        return "${appConfig.netex.outputPath}/${message.getCodespace()}/${message.getCorrelationId()}"
    }

    /**
     * Performs the filtering operation on the Netex file specified in the Pub/Sub message.
     **/
    override fun handleMessage(message: PubsubMessage) {
        try {
            val fileName: String? = message.getNetexFileName()
            val filterProfile = message.getFilterProfile()
            val filterConfig = filterConfigResolver.resolve(filterProfile)
            logger.info("Detected config matching filter profile $filterProfile: $filterConfig")
            filterService.handleFilterRequestForFile(
                fileName = fileName,
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
