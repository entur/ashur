package org.entur.ror.ashur.sax.plugins.activedates.handlers

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesParsingContext
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesRepository
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesDataCollector
import org.xml.sax.Attributes

class DayTypeRefHandler(val activeDatesRepository: ActiveDatesRepository) : ActiveDatesDataCollector() {
    override fun startElement(
        context: ActiveDatesParsingContext,
        attributes: Attributes?,
        currentEntity: Entity
    ) {
        val ref = attributes?.getValue("ref")
        if (currentEntity.type == NetexTypes.DAY_TYPE_ASSIGNMENT) {
            if (ref != null) {
                context.currentDayTypeAssignmentDayTypeRef = ref
            }
        }
        if (currentEntity.type == NetexTypes.SERVICE_JOURNEY) {
            if (ref != null) {
                val serviceJourneyId = currentEntity.id
                activeDatesRepository.getServiceJourneyData(serviceJourneyId).dayTypes.add(ref)
            }
        }
        if (currentEntity.type == NetexTypes.DEAD_RUN) {
            if (ref != null) {
                val deadRunId = currentEntity.id
                activeDatesRepository.getDeadRunData(deadRunId).dayTypes.add(ref)
            }
        }
    }
}