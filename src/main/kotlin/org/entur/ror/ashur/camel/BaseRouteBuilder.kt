package org.entur.ror.ashur.camel

import org.apache.camel.Exchange
import org.apache.camel.LoggingLevel
import org.apache.camel.builder.RouteBuilder
import org.entur.ror.ashur.Constants
import org.entur.ror.ashur.addPubsubAttribute
import org.entur.ror.ashur.config.AppConfig
import org.entur.ror.ashur.exceptions.AshurException
import org.entur.ror.ashur.report.CreateFilteringReportProcessor

open class BaseRouteBuilder(
    val appConfig: AppConfig,
    val netexFilterMessageProcessor: NetexFilterMessageProcessor,
    val createFilteringReportProcessor: CreateFilteringReportProcessor,
): RouteBuilder() {
    val ashurProjectId = appConfig.gcp.ashurProjectId
    val mardukProjectId = appConfig.gcp.mardukProjectId
    val filterSubscription = Constants.FILTER_NETEX_FILE_SUBSCRIPTION
    val statusTopic = Constants.FILTER_NETEX_FILE_STATUS_TOPIC

    override fun configure() {
        val filterSubscription = Constants.FILTER_NETEX_FILE_SUBSCRIPTION
        onException(AshurException::class.java)
            .handled(true)
            .process { exchange ->
                val exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, AshurException::class.java)
                if (exception.errorCode != null) {
                    exchange.addPubsubAttribute(
                        Constants.FILTERING_ERROR_CODE_HEADER,
                        exception.errorCode
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
            .process(createFilteringReportProcessor)
            .log(LoggingLevel.INFO, "Publishing processing status FAILED for codespace: \${header.codespace}")
            .to("google-pubsub:$mardukProjectId:$statusTopic")
    }
}