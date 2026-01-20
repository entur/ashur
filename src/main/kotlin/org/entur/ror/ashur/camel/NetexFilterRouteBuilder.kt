package org.entur.ror.ashur.camel

import org.apache.camel.LoggingLevel
import org.apache.camel.builder.RouteBuilder
import org.entur.ror.ashur.Constants
import org.entur.ror.ashur.config.AppConfig
import org.entur.ror.ashur.getCodespace
import org.entur.ror.ashur.getCorrelationId
import org.entur.ror.ashur.report.CreateFilteringReportProcessor
import org.entur.ror.ashur.toPubsubMessage
import org.springframework.stereotype.Component

/**
 * Entry point for the Camel route that processes messages from a Google Pub/Sub topic.
 *
 * Processes messages sequentially to filter Netex data.
 **/
@Component
class NetexFilterRouteBuilder(
    private val appConfig: AppConfig,
    private val netexFilterMessageProcessor: NetexFilterMessageProcessor,
    private val createFilteringReportProcessor: CreateFilteringReportProcessor,
): RouteBuilder() {
    override fun configure() {
        val ashurProjectId = appConfig.gcp.ashurProjectId
        val mardukProjectId = appConfig.gcp.mardukProjectId

        val filterSubscription = Constants.FILTER_NETEX_FILE_SUBSCRIPTION
        val statusTopic = Constants.FILTER_NETEX_FILE_STATUS_TOPIC

        onException(Exception::class.java)
            .handled(true)
            .log(LoggingLevel.ERROR, "Error processing message from Pub/Sub topic $filterSubscription: \${exception.message} \${exception.stacktrace}")
            .to("direct:filterProcessingStatusFailed")

        from("google-pubsub:$ashurProjectId:${filterSubscription}?synchronousPull=true")
            .log(LoggingLevel.INFO, "Received request to filter Netex from Pub/Sub topic $filterSubscription")
            .process({ exchange ->
                val pubsubMessage = exchange.toPubsubMessage()
                exchange.message.setHeader("codespace", pubsubMessage.getCodespace())
                exchange.message.setHeader("correlationId", pubsubMessage.getCorrelationId())
            })
            .to("direct:filterProcessingStatusStarted")
            .to("direct:filterProcessingQueue")
            .to("direct:filterProcessingStatusSucceeded")
            .routeId("netex-filter-route")

        from("direct:filterProcessingQueue")
            .process(MDCSetupProcessor())
            .log(LoggingLevel.INFO, "Processing request to filter Netex from Pub/Sub topic $filterSubscription")
            .process(netexFilterMessageProcessor)
            .log(LoggingLevel.INFO, "Done processing message from Pub/Sub topic $filterSubscription")
            .onCompletion()
            .process(MDCCleanupProcessor())
            .routeId("netex-filter-processing-route")

        from("direct:filterProcessingStatusStarted")
            .process(SetFilteringStatusProcessor(status = Constants.FILTER_NETEX_FILE_STATUS_STARTED))
            .log(LoggingLevel.INFO, "Publishing processing status STARTED for codespace: \${header.codespace}")
            .to("google-pubsub:$mardukProjectId:$statusTopic")

        from("direct:filterProcessingStatusFailed")
            .process(SetFilteringStatusProcessor(status = Constants.FILTER_NETEX_FILE_STATUS_FAILED))
            .process(createFilteringReportProcessor)
            .log(LoggingLevel.INFO, "Publishing processing status FAILED for codespace: \${header.codespace}")
            .to("google-pubsub:$mardukProjectId:$statusTopic")

        from("direct:filterProcessingStatusSucceeded")
            .process(SetFilteringStatusProcessor(status = Constants.FILTER_NETEX_FILE_STATUS_SUCCEEDED))
            .process(createFilteringReportProcessor)
            .process { exchange ->
                val existingAttributes = exchange
                    .getIn()
                    .getHeader("CamelGooglePubsubAttributes", Map::class.java)
                    .toMutableMap()
                existingAttributes[Constants.FILTERED_NETEX_FILE_PATH_HEADER] = exchange.getIn().getHeader(Constants.FILTERED_NETEX_FILE_PATH_HEADER)
                exchange.getIn().setHeader("CamelGooglePubsubAttributes", existingAttributes)
            }
            .log(LoggingLevel.INFO, "Publishing processing status SUCCEEDED for codespace: \${header.codespace}")
            .to("google-pubsub:$mardukProjectId:$statusTopic")
    }
}