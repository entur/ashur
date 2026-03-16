package org.entur.ror.ashur.sax.plugins.servicejourneynaming.handlers

import org.entur.netex.tools.lib.model.Entity
import org.entur.ror.ashur.sax.plugins.servicejourneynaming.ServiceJourneyNameDataCollector
import org.entur.ror.ashur.sax.plugins.servicejourneynaming.ServiceJourneyNameParsingContext
import org.xml.sax.Attributes

/**
 * Tracks ServiceJourney elements for name collection.
 * This enables JourneyPatternRefHandler and ServiceJourneyNameElementHandler
 * to know which ServiceJourney they're processing.
 */
class ServiceJourneyPluginHandler : ServiceJourneyNameDataCollector() {

    override fun startElement(
        context: ServiceJourneyNameParsingContext,
        attributes: Attributes?,
        currentEntity: Entity
    ) {
        context.currentServiceJourneyId = currentEntity.id
    }

    override fun endElement(
        context: ServiceJourneyNameParsingContext,
        currentEntity: Entity
    ) {
        context.currentServiceJourneyId = null
    }
}
