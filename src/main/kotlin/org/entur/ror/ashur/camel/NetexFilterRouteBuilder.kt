package org.entur.ror.ashur.camel

import org.apache.camel.LoggingLevel
import org.apache.camel.builder.RouteBuilder
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
): RouteBuilder() {
    override fun configure() {
        val projectId = appConfig.pubsub.projectId

        val filterSubscription = Constants.FILTER_NETEX_FILE_SUBSCRIPTION
        val statusSubscription = Constants.FILTER_NETEX_FILE_STATUS_SUBSCRIPTION

        val lastMessageStrategy = LastMessageStrategy(filterSubscription)

        from("google-pubsub:$projectId:${filterSubscription}")
            .onException(Exception::class.java)
                .handled(true)
                .log(LoggingLevel.ERROR, "Error processing message from Pub/Sub topic $filterSubscription: \${exception.message}")
                .to("google-pubsub:$projectId:$statusSubscription")
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
            .to("google-pubsub:$projectId:$statusSubscription")
            .to("direct:filterProcessingQueue")
            .to("google-pubsub:$projectId:$statusSubscription")
            .routeId("netex-filter-route")

        // Note: direct is blocking by default, so only one aggregated message will be processed at a time.
        from("direct:filterProcessingQueue")
            .process(MDCSetupProcessor())
            .log(LoggingLevel.INFO, "Finished aggregating messages for codespace. Processing request to filter Netex from Pub/Sub topic $filterSubscription...")
            .process(netexFilterMessageProcessor)
            .log(LoggingLevel.INFO, "Done processing message from Pub/Sub topic $filterSubscription")
            .onCompletion()
            .process(MDCCleanupProcessor())
            .routeId("sequential-processing-route")
    }
}