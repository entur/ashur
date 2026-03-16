package org.entur.ror.ashur.sax.plugins.servicejourneynaming.handlers

import org.entur.netex.tools.lib.model.Entity
import org.entur.ror.ashur.sax.plugins.servicejourneynaming.ServiceJourneyNameDataCollector
import org.entur.ror.ashur.sax.plugins.servicejourneynaming.ServiceJourneyNameParsingContext
import org.entur.ror.ashur.sax.plugins.servicejourneynaming.ServiceJourneyNameRepository
import org.xml.sax.Attributes

/**
 * Collects DestinationDisplayRef from the first StopPointInJourneyPattern.
 * Maps JourneyPattern ID -> DestinationDisplay ID.
 *
 * Only captures the DestinationDisplayRef from the first stop point (order="1"),
 * as this is the departure stop whose destination display should be used for the Name.
 */
class DestinationDisplayRefHandler(
    private val repository: ServiceJourneyNameRepository
) : ServiceJourneyNameDataCollector() {

    override fun startElement(
        context: ServiceJourneyNameParsingContext,
        attributes: Attributes?,
        currentEntity: Entity
    ) {
        // Only process if we're in the first stop and haven't found a ref yet
        if (context.isFirstStopPoint && !context.foundDestinationDisplayRefForCurrentPattern) {
            val ref = attributes?.getValue("ref")
            val journeyPatternId = context.currentJourneyPatternId

            if (ref != null && journeyPatternId != null) {
                repository.journeyPatternToDestinationDisplay[journeyPatternId] = ref
                context.foundDestinationDisplayRefForCurrentPattern = true
            }
        }
    }
}
