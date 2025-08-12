package org.entur.ror.ashur.camel

import org.apache.camel.LoggingLevel
import org.apache.camel.builder.RouteBuilder
import org.entur.ror.ashur.getCodespace
import org.entur.ror.ashur.getCorrelationId
import org.entur.ror.ashur.toPubsubMessage
import java.util.Properties

/**
 * Entry point for the Camel route that processes messages from a Google Pub/Sub topic.
 *
 * Aggregates messages by codespace and processes them sequentially to filter Netex data.
 **/
class NetexFilterRouteBuilder(
    private val config: Properties
): RouteBuilder() {

    init {
        requireNotNull(config.getProperty("ashur.pubsub.project.id")) { "Project ID is required in configuration" }
        requireNotNull(config.getProperty("subscription.id")) { "Subscription ID is required in configuration" }
    }

    private fun errorHandler() = defaultErrorHandler()
        .maximumRedeliveries(3) // internal retries for failed messages
        .redeliveryDelay(2000) // delay between retries
        .retryAttemptedLogLevel(LoggingLevel.WARN) // log level for retry attempts

    override fun configure() {
        val projectId = config.getProperty("ashur.pubsub.project.id")
        val subscriptionId = config.getProperty("subscription.id")

        val netexFilterMessageProcessor = NetexFilterMessageProcessor(config = config)
        val lastMessageStrategy = LastMessageStrategy(subscriptionId)

        from("google-pubsub:$projectId:$subscriptionId")
            .errorHandler(errorHandler())
            .onException(Exception::class.java)
                .handled(true)
                .log(LoggingLevel.ERROR, "Error processing message from Pub/Sub topic $subscriptionId: \${exception.message}")
                .to("google-pubsub:$projectId:AshurDeadLetterQueue")
            .end()
            .process({ exchange ->
                val pubsubMessage = exchange.toPubsubMessage()
                exchange.message.setHeader("codespace", pubsubMessage.getCodespace())
                exchange.message.setHeader("correlationId", pubsubMessage.getCorrelationId())
            })
            .aggregate(header("codespace"), lastMessageStrategy)
            // fires aggregation if a given group hasn’t received a new message for 5 seconds
            .completionTimeout(5000)
            .parallelProcessing(false)
            .optimisticLocking()
            .to("seda:sequentialProcessingQueue")
            .routeId("netex-filter-route")

        // This queue ensures only one message is processed at a time per pod
        from("seda:sequentialProcessingQueue?concurrentConsumers=1")
            .errorHandler(errorHandler())
            .log(LoggingLevel.INFO, "Finished aggregating messages for codespace ${header("codespace")}")
            .process(MDCSetupProcessor())
            .log(LoggingLevel.INFO, "Processing request to filter Netex from Pub/Sub topic $subscriptionId")
            .process(netexFilterMessageProcessor)
            .log(LoggingLevel.INFO, "Done processing message from Pub/Sub topic $subscriptionId")
            .onCompletion()
            .process(MDCCleanupProcessor())
            .routeId("sequential-processing-route")
    }
}