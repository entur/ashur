package org.entur.ror.ashur.pubsub

import com.google.pubsub.v1.PubsubMessage
import org.entur.ror.ashur.filter.FilterService
import org.entur.ror.ashur.filter.FilterConfigResolver
import org.entur.ror.ashur.filter.FilterContext
import org.entur.ror.ashur.getCorrelationId
import org.entur.ror.ashur.getNetexFileName
import org.entur.ror.ashur.getCodespace
import org.entur.ror.ashur.getFileCreatedTimestamp
import org.entur.ror.ashur.getFilterProfile
import org.entur.ror.ashur.getNetexSource
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Handles incoming messages from a Pub/Sub topic and processes Netex files based on a filter configuration.
 *
 * @param filterService Service responsible for filtering Netex files.
 * @param filterConfigResolver Resolver for obtaining filter configurations based on the filter context.
 */
@Component
class NetexFilterMessageHandler(
    private val filterService: FilterService,
    private val filterConfigResolver: FilterConfigResolver
) : MessageHandler {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Performs the filtering operation on the Netex file specified in the Pub/Sub message.
     *
     * @param message The Pub/Sub message containing details about the Netex file to be filtered.
     * @return The path to the output zip file containing the filtered Netex data.
     **/
    override fun handleMessage(message: PubsubMessage): String {
        val fileName: String? = message.getNetexFileName()
        val filterProfile = message.getFilterProfile()
        val codespace = message.getCodespace()
        val correlationId = message.getCorrelationId()
        val netexSource = message.getNetexSource()
        val fileCreatedTimestamp = message.getFileCreatedTimestamp()

        val filterContext = FilterContext(
            profile = filterProfile,
            codespace = codespace!!,
            fileCreatedAt = fileCreatedTimestamp
        )

        val filterConfig = filterConfigResolver.resolve(filterContext)
        logger.info("Detected config matching filter profile $filterProfile: $filterConfig")

        return filterService.handleFilterRequestForFile(
            fileName = fileName,
            filterConfig = filterConfig,
            codespace = codespace,
            correlationId = correlationId ?: "unknown",
            netexSource = netexSource ?: "unknown",
        )
    }
}
