package org.entur.ror.ashur.camel

import org.apache.camel.LoggingLevel
import org.entur.ror.ashur.Constants
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
): BaseRouteBuilder() {
    override fun configure() {
        val projectId = appConfig.pubsub.projectId

        val filterSubscription = Constants.FILTER_NETEX_FILE_SUBSCRIPTION
        val statusSubscription = Constants.FILTER_NETEX_FILE_STATUS_SUBSCRIPTION

        val lastMessageStrategy = LastMessageStrategy(filterSubscription)

        from("google-pubsub:$projectId:${filterSubscription}")
            .onException(Exception::class.java)
                .handled(true)
                .log(LoggingLevel.ERROR, "Error processing message from Pub/Sub topic $filterSubscription: \${exception.message}")
                .to("direct:filterProcessingStatusFailed")
            .end()
            // Temporarily commented out due to Pub/Sub re-sending messages
            // .process(this::removeSynchronizationForAggregatedExchange)
            .process({ exchange ->
                val pubsubMessage = exchange.toPubsubMessage()
                exchange.message.setHeader("codespace", pubsubMessage.getCodespace())
                exchange.message.setHeader("correlationId", pubsubMessage.getCorrelationId())
            })
            .to("direct:filterProcessingStatusStarted")
            .aggregate(header("codespace"), lastMessageStrategy)
            .completionTimeout(1000)
            // Temporarily commented out due to Pub/Sub re-sending messages
            // .process(this::addSynchronizationForAggregatedExchange)
            .parallelProcessing(false)
            .optimisticLocking()
            .log(LoggingLevel.INFO, "Aggregated messages for codespace: \${header.codespace}. Sending to filter processing queue...")
            .to("direct:filterProcessingQueue")
            .to("direct:filterProcessingStatusSucceeded")
            .routeId("netex-filter-route")

        // Note: direct is blocking by default, so only one aggregated message will be processed at a time.
        from("direct:filterProcessingQueue")
            .process(MDCSetupProcessor())
            .log(LoggingLevel.INFO, "Processing request to filter Netex from Pub/Sub topic $filterSubscription...")
            .process(netexFilterMessageProcessor)
            .log(LoggingLevel.INFO, "Done processing message from Pub/Sub topic $filterSubscription")
            .onCompletion()
            .process(MDCCleanupProcessor())
            .routeId("sequential-processing-route")

        from("direct:filterProcessingStatusStarted")
            .setHeader("status", constant("STARTED"))
            .log(LoggingLevel.INFO, "Publishing processing status STARTED for codespace: \${header.codespace}")
            .to("google-pubsub:$projectId:$statusSubscription")

        from("direct:filterProcessingStatusFailed")
            .setHeader("status", constant("FAILED"))
            .log(LoggingLevel.INFO, "Publishing processing status FAILED for codespace: \${header.codespace}")
            .to("google-pubsub:$projectId:$statusSubscription")

        from("direct:filterProcessingStatusSucceeded")
            .setHeader("status", constant("SUCCEEDED"))
            .log(LoggingLevel.INFO, "Publishing processing status SUCCEEDED for codespace: \${header.codespace}")
            .to("google-pubsub:$projectId:$statusSubscription")
    }
}