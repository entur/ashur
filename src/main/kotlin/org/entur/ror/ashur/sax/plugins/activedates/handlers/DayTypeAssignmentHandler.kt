package org.entur.ror.ashur.sax.plugins.activedates.handlers

import org.entur.netex.tools.lib.model.Entity
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesParsingContext
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesRepository
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesDataCollector

class DayTypeAssignmentHandler(
    val activeDatesRepository: ActiveDatesRepository
) : ActiveDatesDataCollector() {

    override fun endElement(context: ActiveDatesParsingContext, currentEntity: Entity) {
        if (context.currentDayTypeAssignmentDayTypeRef == null) {
            return
        }
        context.currentDayTypeAssignmentOperatingDay?.let {
            activeDatesRepository.getDayTypeData(context.currentDayTypeAssignmentDayTypeRef!!)
                .operatingDays.add(it)
        }

        context.currentDayTypeAssignmentOperatingPeriod?.let {
            activeDatesRepository.getDayTypeData(context.currentDayTypeAssignmentDayTypeRef!!)
                .operatingPeriods.add(it)
        }

        context.currentDayTypeAssignmentDate?.let {
            activeDatesRepository.getDayTypeData(context.currentDayTypeAssignmentDayTypeRef!!)
                .dates.add(it)
            activeDatesRepository.dayTypeAssignmentToDate.put(currentEntity.id, it)
        }

        context.currentDayTypeAssignmentDayTypeRef = null
        context.currentDayTypeAssignmentOperatingDay = null
        context.currentDayTypeAssignmentOperatingPeriod = null
        context.currentDayTypeAssignmentDate = null
    }
}