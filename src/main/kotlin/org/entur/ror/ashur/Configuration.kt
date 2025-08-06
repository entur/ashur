package org.entur.ror.ashur

import com.google.cloud.pubsub.v1.AckReplyConsumer
import com.google.pubsub.v1.PubsubMessage
import org.entur.ror.ashur.gcp.GcsClient
import org.entur.ror.ashur.pubsub.GooglePubSubListener
import org.entur.ror.ashur.pubsub.MessageHandler
import org.entur.ror.ashur.pubsub.PubSubEmulatorListener
import org.entur.ror.ashur.pubsub.PubSubListener
import org.entur.ror.ashur.file.FileService
import org.entur.ror.ashur.file.GcsFileService
import org.entur.ror.ashur.file.LocalFileService
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import java.util.Properties

fun getConfiguration(): Properties {
    val logger = LoggerFactory.getLogger("Configuration")
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

/**
 * Wraps the message handling with MDC context for logging.
 *
 * @param message The Pub/Sub message to handle.
 * @param consumerWithResponse The consumer to acknowledge or nack the message.
 * @param messageHandler The actual message handler to process the message.
 */
fun wrapWithMDC(message: PubsubMessage, consumerWithResponse: AckReplyConsumer, messageHandler: MessageHandler) {
    MDC.put("correlationId", message.getCorrelationId() ?: "unknown")
    MDC.put("codespace", message.getCodespace() ?: "unknown")
    try {
        messageHandler.handleMessage(message, consumerWithResponse)
    } finally {
        MDC.remove("correlationId")
        MDC.remove("codespace")
    }
}

fun setupPubsubListener(
    messageHandler: MessageHandler,
    config: Properties
): PubSubListener {
    // Ensures correlationId and codespace are set in MDC for each message
    val mdcWrappedHandler = object : MessageHandler {
        override fun handleMessage(message: PubsubMessage, consumer: AckReplyConsumer) {
            wrapWithMDC(message, consumer, messageHandler)
        }
    }

    val pubSubServiceType = config.getProperty("pubsub.service.type")
    val projectId = config.getProperty("project.id")
    val subscriptionId = config.getProperty("subscription.id")
    val emulatorHost = config.getProperty("emulator.host")
    return when (pubSubServiceType) {
        "emulator" -> PubSubEmulatorListener(
            projectId = projectId,
            subscriptionId = subscriptionId,
            emulatorHost = emulatorHost,
            messageHandler = mdcWrappedHandler::handleMessage
        )
        "gcp" -> GooglePubSubListener(
            projectId = projectId,
            subscriptionId = subscriptionId,
            messageHandler = mdcWrappedHandler::handleMessage
        )
        else -> throw IllegalArgumentException("Unknown Pub/Sub service type: $pubSubServiceType")
    }
}