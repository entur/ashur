package org.entur.ror.ashur.camel

import org.apache.camel.LoggingLevel
import org.apache.camel.builder.RouteBuilder
import java.util.Properties

class NetexFilterRouteBuilder(
    private val config: Properties
): RouteBuilder() {

    init {
        requireNotNull(config.getProperty("ashur.pubsub.project.id")) { "Project ID is required in configuration" }
        requireNotNull(config.getProperty("subscription.id")) { "Subscription ID is required in configuration" }
    }

    override fun configure() {
        val projectId = config.getProperty("ashur.pubsub.project.id")
        val subscriptionId = config.getProperty("subscription.id")

        val netexFilterMessageProcessor = NetexFilterMessageProcessor(config = config)

        from("google-pubsub:$projectId:$subscriptionId")
            .process(MDCSetupProcessor())
            .log(LoggingLevel.INFO, "Received message from Pub/Sub topic FilterNetexFileQueue")
            .process(netexFilterMessageProcessor)
            .log(LoggingLevel.INFO, "Done processing message from Pub/Sub topic FilterNetexFileQueue")
            .onCompletion()
            .process(MDCCleanupProcessor())
            .routeId("netex-filter-route")
    }

}