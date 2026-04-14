package org.entur.ror.ashur.sax.plugins.journeypatternname.handlers

import org.entur.netex.tools.lib.model.Entity
import org.entur.ror.ashur.sax.NetexElementNames.JOURNEY_PATTERN
import org.entur.ror.ashur.sax.plugins.journeypatternname.JourneyPatternNameDataCollector
import org.entur.ror.ashur.sax.plugins.journeypatternname.JourneyPatternNameRepository
import org.xml.sax.Attributes

class RouteRefHandler(
    private val repository: JourneyPatternNameRepository
) : JourneyPatternNameDataCollector() {

    override fun startElement(attributes: Attributes?, currentEntity: Entity) {
        if (currentEntity.type == JOURNEY_PATTERN) {
            val routeRef = attributes?.getValue("ref")
            if (routeRef != null) {
                repository.journeyPatternRouteRefs[currentEntity.id] = routeRef
            }
        }
    }
}
