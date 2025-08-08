package org.entur.ror.ashur

import org.apache.camel.component.properties.PropertiesComponent
import org.apache.camel.main.Main
import org.entur.ror.ashur.camel.NetexFilterRouteBuilder

fun main() {
    configureLogback()

    val config = getConfiguration()

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