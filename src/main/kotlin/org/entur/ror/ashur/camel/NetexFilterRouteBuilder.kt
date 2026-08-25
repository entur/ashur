package org.entur.ror.ashur.camel

import org.apache.camel.LoggingLevel
import org.entur.ror.ashur.Constants
import org.entur.ror.ashur.addPubsubAttribute
import org.entur.ror.ashur.config.AppConfig
import org.entur.ror.ashur.getCodespace
import org.entur.ror.ashur.getCorrelationId
import org.entur.ror.ashur.metrics.FilterMetrics
import org.entur.ror.ashur.metrics.RecordFilterRunProcessor
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
    appConfig: AppConfig,
    netexFilterMessageProcessor: NetexFilterMessageProcessor,
    createFilteringReportProcessor: CreateFilteringReportProcessor,
    filterMetrics: FilterMetrics,
    private val redeliveryGuardProcessor: RedeliveryGuardProcessor,
    private val redeliveryGuardCompletionProcessor: RedeliveryGuardCompletionProcessor,
) : BaseRouteBuilder(appConfig, netexFilterMessageProcessor, createFilteringReportProcessor, filterMetrics) {
    override fun configure() {
        super.configure()

        // maxDeliveryAttempts=0 disables Camel's client-side pre-route nack gate (added in
        // camel-google-pubsub 4.18). Pub/Sub's own server-side redelivery and dead-lettering
        // are unaffected. Set explicitly to suppress the auto-fetch attempt that runs otherwise.
        from("google-pubsub:$ashurProjectId:${filterSubscription}?synchronousPull=true&maxDeliveryAttempts=0")
            .log(LoggingLevel.INFO, "Received request to filter Netex from Pub/Sub topic $filterSubscription")
            .process({ exchange ->
                val pubsubMessage = exchange.toPubsubMessage()
                exchange.message.setHeader("codespace", pubsubMessage.getCodespace())
                exchange.message.setHeader("correlationId", pubsubMessage.getCorrelationId())
                exchange.message.setHeader(Constants.FILTERING_PROFILE_HEADER,
                    pubsubMessage.attributesMap[Constants.FILTERING_PROFILE_HEADER])
            })
            // Idempotency guard: runs before STARTED so a skipped/bounced redelivery emits no status.
            // SKIP -> stop (ack, no re-processing); BOUNCE -> ClaimHeldException (nack); PROCESS -> continue.
            .process(redeliveryGuardProcessor)
            .choice()
                .`when`(header(Constants.GUARD_DECISION_HEADER).isEqualTo(Constants.GUARD_DECISION_SKIP))
                    .log(LoggingLevel.INFO, "Redelivery guard: skipping already-completed request for codespace: \${header.codespace}")
                    .stop()
            .end()
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

        from("direct:filterProcessingStatusSucceeded")
            .process(SetFilteringStatusProcessor(status = Constants.FILTER_NETEX_FILE_STATUS_SUCCEEDED))
            .process(RecordFilterRunProcessor(filterMetrics, successful = true))
            .process(createFilteringReportProcessor)
            .process { exchange ->
                exchange.getIn()
                    .getHeader(Constants.FILTERED_NETEX_FILE_PATH_HEADER, String::class.java)
                    ?.let { exchange.addPubsubAttribute(Constants.FILTERED_NETEX_FILE_PATH_HEADER, it) }
            }
            .log(LoggingLevel.INFO, "Publishing processing status SUCCEEDED for codespace: \${header.codespace}")
            .to("google-pubsub:$mardukProjectId:$statusTopic")
            // Only after SUCCEEDED has actually been published is the run's every externally-visible
            // effect done; only then can the claim be marked completed (see RedeliveryGuard).
            .process(redeliveryGuardCompletionProcessor)
    }
}