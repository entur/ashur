package org.entur.ror.ashur.camel

import org.apache.camel.LoggingLevel
import org.apache.camel.builder.RouteBuilder
import org.entur.ror.ashur.config.AppConfig
import org.entur.ror.ashur.getCodespace
import org.entur.ror.ashur.getCorrelationId
import org.entur.ror.ashur.toPubsubMessage
import org.springframework.stereotype.Component

/**
 * Entry point for the Camel route that processes messages from a Google Pub/Sub topic.
 *
 * Aggregates messages by codespace and processes them sequentially to filter Netex data.
 **/
@Component
class NetexFilterRouteBuilder(
    private val appConfig: AppConfig,
    private val netexFilterMessageProcessor: NetexFilterMessageProcessor
): RouteBuilder() {

    private fun errorHandler() = defaultErrorHandler()
        .maximumRedeliveries(3) // internal retries for failed messages
        .redeliveryDelay(2000) // delay between retries
        .retryAttemptedLogLevel(LoggingLevel.WARN) // log level for retry attempts

    override fun configure() {
        val projectId = appConfig.pubsub.projectId
        val subscription = appConfig.pubsub.subscription
        val lastMessageStrategy = LastMessageStrategy(subscription)

        from("google-pubsub:$projectId:$subscription")
            .errorHandler(errorHandler())
            .onException(Exception::class.java)
                .handled(true)
                .log(LoggingLevel.ERROR, "Error processing message from Pub/Sub topic $subscription: \${exception.message}")
                .to("google-pubsub:$projectId:${subscription}DeadLetterQueue")
            .end()
            .process({ exchange ->
                val pubsubMessage = exchange.toPubsubMessage()
                exchange.message.setHeader("codespace", pubsubMessage.getCodespace())
                exchange.message.setHeader("correlationId", pubsubMessage.getCorrelationId())
            })
            .aggregate(header("codespace"), lastMessageStrategy)
            .completionTimeout(1000)
            .parallelProcessing(false)
            .optimisticLocking()
            .to("seda:sequentialProcessingQueue")
            .routeId("netex-filter-route")

        // This queue ensures only one message is processed at a time per pod
        from("seda:sequentialProcessingQueue?concurrentConsumers=1")
            .errorHandler(errorHandler())
            .log(LoggingLevel.INFO, "Finished aggregating messages for codespace ${header("codespace")}")
            .process(MDCSetupProcessor())
            .log(LoggingLevel.INFO, "Processing request to filter Netex from Pub/Sub topic $subscription")
            .process(netexFilterMessageProcessor)
            .log(LoggingLevel.INFO, "Done processing message from Pub/Sub topic $subscription")
            .onCompletion()
            .process(MDCCleanupProcessor())
            .routeId("sequential-processing-route")
    }
}