package org.entur.ror.ashur.sax.plugins.servicejourneynaming.handlers

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.ror.ashur.sax.plugins.servicejourneynaming.ServiceJourneyNameDataCollector
import org.entur.ror.ashur.sax.plugins.servicejourneynaming.ServiceJourneyNameParsingContext
import org.entur.ror.ashur.sax.plugins.servicejourneynaming.ServiceJourneyNameRepository
import org.xml.sax.Attributes

/**
 * Collects JourneyPatternRef from ServiceJourney elements.
 * Maps ServiceJourney ID -> JourneyPattern ID.
 */
class JourneyPatternRefHandler(
    private val repository: ServiceJourneyNameRepository
) : ServiceJourneyNameDataCollector() {

    override fun startElement(
        context: ServiceJourneyNameParsingContext,
        attributes: Attributes?,
        currentEntity: Entity
    ) {
        // Only process if we're inside a ServiceJourney
        if (currentEntity.type == NetexTypes.SERVICE_JOURNEY) {
            val ref = attributes?.getValue("ref")
            val serviceJourneyId = context.currentServiceJourneyId

            if (ref != null && serviceJourneyId != null) {
                repository.serviceJourneyToJourneyPattern[serviceJourneyId] = ref
            }
        }
    }
}
