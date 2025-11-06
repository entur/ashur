package org.entur.ror.ashur.pubsub

import com.google.pubsub.v1.PubsubMessage
import org.entur.ror.ashur.filter.FilterService
import org.entur.ror.ashur.filter.FilterConfigResolver
import org.entur.ror.ashur.getCorrelationId
import org.entur.ror.ashur.getNetexFileName
import org.entur.ror.ashur.getCodespace
import org.entur.ror.ashur.getFilterProfile
import org.entur.ror.ashur.getNetexSource
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Handles incoming messages from a Pub/Sub topic and processes Netex files based on a filter configuration.
 *
 * @param config The configuration properties for the message handler.
 */
@Component
class NetexFilterMessageHandler(
    private val filterService: FilterService,
    private val filterConfigResolver: FilterConfigResolver
): MessageHandler {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Performs the filtering operation on the Netex file specified in the Pub/Sub message.
     *
     * @return The path to the output zip file containing the filtered Netex data.
     **/
    override fun handleMessage(message: PubsubMessage): String {
        try {
            val fileName: String? = message.getNetexFileName()
            val filterProfile = message.getFilterProfile()
            val codespace = message.getCodespace()
            val correlationId = message.getCorrelationId()
            val netexSource = message.getNetexSource()

            val filterConfig = filterConfigResolver.resolve(filterProfile, codespace ?: "")
            logger.info("Detected config matching filter profile $filterProfile: $filterConfig")

            return filterService.handleFilterRequestForFile(
                fileName = fileName,
                filterConfig = filterConfig,
                codespace = codespace ?: "unknown",
                correlationId = correlationId ?: "unknown",
                netexSource = netexSource ?: "unknown",
            )
        } catch (e: Exception) {
            logger.error("Exception occurred while processing message", e)
            throw e
        }
    }
}
