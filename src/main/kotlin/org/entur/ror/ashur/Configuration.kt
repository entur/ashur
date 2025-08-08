package org.entur.ror.ashur

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import org.entur.ror.ashur.gcp.GcsClient
import org.entur.ror.ashur.file.FileService
import org.entur.ror.ashur.file.GcsFileService
import org.entur.ror.ashur.file.LocalFileService
import org.slf4j.LoggerFactory
import java.io.File
import java.util.Properties

fun configureLogback(logbackXmlPath: String = System.getProperty("logging.config")) {
    val context = LoggerFactory.getILoggerFactory() as LoggerContext
    val configurator = JoranConfigurator()
    configurator.context = context
    context.reset()
    configurator.doConfigure(File(logbackXmlPath))
    LoggerFactory.getLogger("Configuration").info("Logback configured with file: $logbackXmlPath")
}

fun getConfiguration(): Properties {
    val logger = LoggerFactory.getLogger("Configuration:")
    val properties = Properties()
    val configFile = System.getProperty("config.file")
    val inputStream = if (configFile != null) {
        logger.info("Using config file from path: $configFile")
        java.io.FileInputStream(configFile)
    } else {
        logger.info("Using config file from classpath: $configFile")
        object {}.javaClass.getResourceAsStream("/application.properties")
    }
    inputStream.use { properties.load(it) }
    return properties
}

fun setupFileService(properties: Properties): FileService {
    val fileServiceType = properties.getProperty("file.service.type")
    val bucketName = properties.getProperty("gcp.bucket.name")
    return when (fileServiceType) {
        "local" -> LocalFileService()
        "gcp" -> GcsFileService(gcsClient = GcsClient(), bucketName = bucketName)
        else -> throw IllegalArgumentException("Unknown file service type: $fileServiceType")
    }
}
