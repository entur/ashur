package org.entur.ror.ashur.camel

import org.apache.camel.Exchange
import org.apache.camel.LoggingLevel
import org.apache.camel.builder.RouteBuilder
import org.entur.ror.ashur.Constants
import org.entur.ror.ashur.addPubsubAttribute
import org.entur.ror.ashur.config.AppConfig
import org.entur.ror.ashur.exceptions.AshurException
import org.entur.ror.ashur.exceptions.ClaimHeldException
import org.entur.ror.ashur.metrics.FilterMetrics
import org.entur.ror.ashur.metrics.RecordFilterRunProcessor
import org.entur.ror.ashur.report.CreateFilteringReportProcessor

open class BaseRouteBuilder(
    val appConfig: AppConfig,
    val netexFilterMessageProcessor: NetexFilterMessageProcessor,
    val createFilteringReportProcessor: CreateFilteringReportProcessor,
    val filterMetrics: FilterMetrics,
): RouteBuilder() {
    val ashurProjectId = appConfig.gcp.ashurProjectId
    val mardukProjectId = appConfig.gcp.mardukProjectId
    val filterSubscription = Constants.FILTER_NETEX_FILE_SUBSCRIPTION
    val statusTopic = Constants.FILTER_NETEX_FILE_STATUS_TOPIC

    override fun configure() {
        val filterSubscription = Constants.FILTER_NETEX_FILE_SUBSCRIPTION

        interceptFrom("google-pubsub:*")
            .process(LogInboundPubsubMessageProcessor())

        interceptSendToEndpoint("google-pubsub:*")
            .process(LogOutboundPubsubMessageProcessor())

        // A bounce is NOT a failure: do not publish FAILED, and handled(false) lets the exception
        // propagate so the Pub/Sub consumer nacks the message and the server redelivers it later.
        //
        // Without the log* suppressions below, every bounce also went through the default error
        // handler's "Failed delivery ... Exhausted after delivery attempt: 1" at ERROR, with a stack
        // trace. A run whose duration exceeds the ack deadline bounces repeatedly, so a perfectly
        // healthy run produced a burst of ERROR entries. These are scoped to ClaimHeldException, so
        // genuine failures still log in full.
        onException(ClaimHeldException::class.java)
            .handled(false)
            .logExhausted(false)
            .logStackTrace(false)
            .logExhaustedMessageHistory(false)
            .log(
                LoggingLevel.INFO,
                "Redelivery guard bounced message from Pub/Sub topic $filterSubscription (nack for redelivery): \${exception.message}",
            )

        onException(AshurException::class.java)
            .handled(true)
            .process { exchange ->
                val errorCode = exchange
                    .getProperty(Exchange.EXCEPTION_CAUGHT, AshurException::class.java)
                    ?.errorCode
                if (errorCode != null) {
                    exchange.addPubsubAttribute(
                        Constants.FILTERING_ERROR_CODE_HEADER,
                        errorCode
                    )
                }
            }
            .log(
                LoggingLevel.ERROR,
                "Error occured when processing message from Pub/Sub topic $filterSubscription: \${exception.message} \${exception.stacktrace}"
            )
            .to("direct:filterProcessingStatusFailed")

        onException(Exception::class.java)
            .handled(true)
            .process { exchange ->
                exchange.addPubsubAttribute(Constants.FILTERING_FAILURE_REASON_HEADER, "Unexpected system error")
            }
            .log(
                LoggingLevel.ERROR,
                "Unexpected error occured when processing message from Pub/Sub topic $filterSubscription: \${exception.message} \${exception.stacktrace}"
            )
            .to("direct:filterProcessingStatusFailed")

        from("direct:filterProcessingStatusFailed")
            .process(SetFilteringStatusProcessor(status = Constants.FILTER_NETEX_FILE_STATUS_FAILED))
            .process(RecordFilterRunProcessor(filterMetrics, successful = false))
            .process(createFilteringReportProcessor)
            .log(LoggingLevel.INFO, "Publishing processing status FAILED for codespace: \${header.codespace}")
            .to("google-pubsub:$mardukProjectId:$statusTopic")
    }
}