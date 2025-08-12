package org.entur.ror.ashur.filter

import com.google.pubsub.v1.PubsubMessage
import org.entur.netex.tools.pipeline.config.CliConfig
import org.entur.netex.tools.pipeline.config.JsonConfig
import org.entur.ror.ashur.getConfiguration
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.util.Properties

/**
 * Loader for filter configuration.
 *
 * This class is responsible for loading the filter configuration either from a local resource file
 * or from a Pub/Sub message. It checks the configuration to determine the source of the filter
 * configuration and loads it accordingly.
 */
class FilterConfigLoader(private val config: Properties = getConfiguration()) {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun loadFromInputStream(inputStream: InputStream): CliConfig? =
        JsonConfig.load(inputStream)

    fun loadFilterConfig(message: PubsubMessage): CliConfig? {
        val useLocalFilterConfig = config.getProperty("useLocalFilterConfig")?.toBoolean() ?: false
        if (useLocalFilterConfig) {
            val resourceStream = javaClass.classLoader.getResourceAsStream("config.json")
            if (resourceStream != null) {
                logger.info("Loading filter config from local resource: config.json")
                return loadFromInputStream(resourceStream)
            } else {
                throw IllegalStateException("Local filter config file not found in classpath.")
            }
        } else {
            val filterConfigJson = message.data.newInput()
            logger.info("Loading filter config from Pub/Sub message data.")
            return loadFromInputStream(filterConfigJson)
        }
    }
}