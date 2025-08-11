package org.entur.ror.ashur

import org.apache.camel.component.properties.PropertiesComponent
import org.apache.camel.main.Main
import org.entur.ror.ashur.camel.NetexFilterRouteBuilder
import org.entur.ror.ashur.utils.subscriptionExists
import org.slf4j.LoggerFactory

fun main() {
    configureLogback()
    val logger = LoggerFactory.getLogger("Startup:")

    val config = getConfiguration()

    val projectId = config.getProperty("ashur.pubsub.project.id")
    val subscriptionId = config.getProperty("subscription.id")
    val emulatorHost = System.getenv("PUBSUB_EMULATOR_HOST")

    if (!subscriptionExists(projectId, subscriptionId, emulatorHost)) {
        logger.error("Subscription $subscriptionId does not exist in project $projectId. Exiting...")
        return
    }

    val main = Main()
    configureCamelProperties(main)
    val netexFilterRouteBuilder = NetexFilterRouteBuilder(config = config)
    main.configure().addRoutesBuilder(netexFilterRouteBuilder)
    main.run()
}

fun configureCamelProperties(main: Main) {
    val propertiesComponent = PropertiesComponent()
    propertiesComponent.location = "file:${System.getProperty("config.file")}"
    main.bind("properties", propertiesComponent)
}