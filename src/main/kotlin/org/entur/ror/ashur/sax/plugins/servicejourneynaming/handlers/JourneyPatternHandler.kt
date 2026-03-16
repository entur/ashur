package org.entur.ror.ashur.sax.plugins.servicejourneynaming.handlers

import org.entur.netex.tools.lib.model.Entity
import org.entur.ror.ashur.sax.plugins.servicejourneynaming.ServiceJourneyNameDataCollector
import org.entur.ror.ashur.sax.plugins.servicejourneynaming.ServiceJourneyNameParsingContext
import org.xml.sax.Attributes

/**
 * Tracks the current JourneyPattern being parsed.
 * This enables DestinationDisplayRefHandler to associate the ref with the correct JourneyPattern.
 */
class JourneyPatternHandler : ServiceJourneyNameDataCollector() {

    override fun startElement(
        context: ServiceJourneyNameParsingContext,
        attributes: Attributes?,
        currentEntity: Entity
    ) {
        context.currentJourneyPatternId = currentEntity.id
        context.foundDestinationDisplayRefForCurrentPattern = false
    }

    override fun endElement(
        context: ServiceJourneyNameParsingContext,
        currentEntity: Entity
    ) {
        context.currentJourneyPatternId = null
        context.foundDestinationDisplayRefForCurrentPattern = false
    }
}
