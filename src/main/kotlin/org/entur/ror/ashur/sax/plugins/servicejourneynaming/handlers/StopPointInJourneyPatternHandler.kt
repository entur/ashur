package org.entur.ror.ashur.sax.plugins.servicejourneynaming.handlers

import org.entur.netex.tools.lib.model.Entity
import org.entur.ror.ashur.sax.plugins.servicejourneynaming.ServiceJourneyNameDataCollector
import org.entur.ror.ashur.sax.plugins.servicejourneynaming.ServiceJourneyNameParsingContext
import org.xml.sax.Attributes

/**
 * Tracks StopPointInJourneyPattern elements to identify the first stop (order="1").
 * The first stop is the departure stop, whose DestinationDisplay provides the FrontText.
 */
class StopPointInJourneyPatternHandler : ServiceJourneyNameDataCollector() {

    override fun startElement(
        context: ServiceJourneyNameParsingContext,
        attributes: Attributes?,
        currentEntity: Entity
    ) {
        val order = attributes?.getValue("order")
        context.isFirstStopPoint = (order == "1")
    }

    override fun endElement(
        context: ServiceJourneyNameParsingContext,
        currentEntity: Entity
    ) {
        context.isFirstStopPoint = false
    }
}
