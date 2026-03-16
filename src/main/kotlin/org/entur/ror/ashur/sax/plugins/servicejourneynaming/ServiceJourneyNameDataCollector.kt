package org.entur.ror.ashur.sax.plugins.servicejourneynaming

import org.entur.netex.tools.lib.model.Entity
import org.xml.sax.Attributes

/**
 * Base class for data collectors used by ServiceJourneyNamePlugin.
 * Follows the pattern from ActiveDatesDataCollector.
 */
abstract class ServiceJourneyNameDataCollector {
    open fun startElement(
        context: ServiceJourneyNameParsingContext,
        attributes: Attributes?,
        currentEntity: Entity
    ) {}

    open fun characters(
        context: ServiceJourneyNameParsingContext,
        ch: CharArray?,
        start: Int,
        length: Int
    ) {}

    open fun endElement(
        context: ServiceJourneyNameParsingContext,
        currentEntity: Entity
    ) {}
}
