package org.entur.ror.ashur.filter

import org.entur.netex.tools.pipeline.config.CliConfig
import org.entur.netex.tools.pipeline.config.JsonConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.InputStream

/**
 * Loader for filter configuration.
 *
 * This class is responsible for loading the filter configuration either from a local resource file
 * or from a Pub/Sub message. It checks the configuration to determine the source of the filter
 * configuration and loads it accordingly.
 */
class FilterConfigLoader() {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun loadFromInputStream(inputStream: InputStream): CliConfig? =
        JsonConfig.load(inputStream)

    fun loadFilterConfig(): CliConfig? {
        val resourceStream = javaClass.classLoader.getResourceAsStream("config.json")
        if (resourceStream != null) {
            logger.info("Loading filter config from local resource: config.json")
            return loadFromInputStream(resourceStream)
        } else {
            throw IllegalStateException("Local filter config file not found in classpath.")
        }
    }
}