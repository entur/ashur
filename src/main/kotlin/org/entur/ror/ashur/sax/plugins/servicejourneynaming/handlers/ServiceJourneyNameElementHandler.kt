package org.entur.ror.ashur.sax.plugins.servicejourneynaming.handlers

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.ror.ashur.sax.plugins.servicejourneynaming.ServiceJourneyNameDataCollector
import org.entur.ror.ashur.sax.plugins.servicejourneynaming.ServiceJourneyNameParsingContext
import org.entur.ror.ashur.sax.plugins.servicejourneynaming.ServiceJourneyNameRepository
import org.xml.sax.Attributes

/**
 * Detects existing Name elements inside ServiceJourney to avoid duplication.
 * Records ServiceJourney IDs that already have a Name element.
 */
class ServiceJourneyNameElementHandler(
    private val repository: ServiceJourneyNameRepository
) : ServiceJourneyNameDataCollector() {

    override fun startElement(
        context: ServiceJourneyNameParsingContext,
        attributes: Attributes?,
        currentEntity: Entity
    ) {
        // If we're inside a ServiceJourney and encounter a Name element, record it
        if (currentEntity.type == NetexTypes.SERVICE_JOURNEY) {
            val serviceJourneyId = context.currentServiceJourneyId
            if (serviceJourneyId != null) {
                repository.serviceJourneysWithName.add(serviceJourneyId)
            }
        }
    }
}
