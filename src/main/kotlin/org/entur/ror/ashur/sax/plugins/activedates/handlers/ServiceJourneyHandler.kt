package org.entur.ror.ashur.sax.plugins.activedates.handlers

import org.entur.netex.tools.lib.model.Entity
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesParsingContext
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesRepository
import org.entur.ror.ashur.sax.plugins.activedates.NetexDataCollector
import org.xml.sax.Attributes

class ServiceJourneyHandler(val activeDatesRepository: ActiveDatesRepository): NetexDataCollector() {
    override fun startElement(
        context: ActiveDatesParsingContext,
        attributes: Attributes?,
        currentEntity: Entity
    ) {
        context.currentServiceJourneyId = currentEntity.id
    }

    override fun endElement(context: ActiveDatesParsingContext, currentEntity: Entity) {
        context.currentServiceJourneyId = null
    }
}