package org.entur.ror.ashur.sax.plugins.servicejourneynaming.handlers

import org.entur.netex.tools.lib.model.Entity
import org.entur.ror.ashur.sax.plugins.servicejourneynaming.ServiceJourneyNameDataCollector
import org.entur.ror.ashur.sax.plugins.servicejourneynaming.ServiceJourneyNameParsingContext
import org.xml.sax.Attributes

/**
 * Handles DestinationDisplay elements to track current DestinationDisplay ID.
 * This enables FrontTextHandler to know which DestinationDisplay it's collecting text for.
 */
class DestinationDisplayHandler : ServiceJourneyNameDataCollector() {

    override fun startElement(
        context: ServiceJourneyNameParsingContext,
        attributes: Attributes?,
        currentEntity: Entity
    ) {
        context.currentDestinationDisplayId = currentEntity.id
    }

    override fun endElement(
        context: ServiceJourneyNameParsingContext,
        currentEntity: Entity
    ) {
        context.currentDestinationDisplayId = null
    }
}
