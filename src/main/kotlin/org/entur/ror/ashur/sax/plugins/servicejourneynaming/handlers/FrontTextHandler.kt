package org.entur.ror.ashur.sax.plugins.servicejourneynaming.handlers

import org.entur.netex.tools.lib.model.Entity
import org.entur.ror.ashur.sax.plugins.servicejourneynaming.ServiceJourneyNameDataCollector
import org.entur.ror.ashur.sax.plugins.servicejourneynaming.ServiceJourneyNameParsingContext
import org.entur.ror.ashur.sax.plugins.servicejourneynaming.ServiceJourneyNameRepository

/**
 * Collects FrontText content from DestinationDisplay elements.
 * Maps DestinationDisplay ID -> FrontText value.
 */
class FrontTextHandler(
    private val repository: ServiceJourneyNameRepository
) : ServiceJourneyNameDataCollector() {

    private val stringBuilder = StringBuilder()

    override fun characters(
        context: ServiceJourneyNameParsingContext,
        ch: CharArray?,
        start: Int,
        length: Int
    ) {
        if (ch != null) {
            stringBuilder.append(ch, start, length)
        }
    }

    override fun endElement(
        context: ServiceJourneyNameParsingContext,
        currentEntity: Entity
    ) {
        // Only store if we're inside a DestinationDisplay
        val destinationDisplayId = context.currentDestinationDisplayId
        if (destinationDisplayId != null) {
            val frontText = stringBuilder.toString().trim()
            if (frontText.isNotEmpty()) {
                repository.destinationDisplayFrontText[destinationDisplayId] = frontText
            }
        }
        stringBuilder.setLength(0)
    }
}
