package org.entur.ror.ashur.sax.plugins.activedates.handlers

import org.entur.netex.tools.lib.model.Entity
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesParsingContext
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesRepository
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesDataCollector

class DayTypeAssignmentHandler(
    val activeDatesRepository: ActiveDatesRepository
) : ActiveDatesDataCollector() {

    override fun endElement(context: ActiveDatesParsingContext, currentEntity: Entity) {
        val dayTypeId = context.currentDayTypeAssignmentDayTypeRef
        if (dayTypeId == null) {
            resetContext(context)
            return
        }

        val isAvailable = context.currentDayTypeAssignmentIsAvailable
        val dayTypeData = activeDatesRepository.getDayTypeData(dayTypeId)

        context.currentDayTypeAssignmentOperatingDay?.let { operatingDayId ->
            if (isAvailable) {
                dayTypeData.operatingDays.add(operatingDayId)
            } else {
                dayTypeData.excludedOperatingDays.add(operatingDayId)
            }
        }

        context.currentDayTypeAssignmentOperatingPeriod?.let { operatingPeriodId ->
            if (isAvailable) {
                dayTypeData.operatingPeriods.add(operatingPeriodId)
            } else {
                dayTypeData.excludedOperatingPeriods.add(operatingPeriodId)
            }
        }

        context.currentDayTypeAssignmentDate?.let { date ->
            activeDatesRepository.dayTypeAssignmentToDate[currentEntity.id] = date
            if (isAvailable) {
                dayTypeData.dates.add(date)
                activeDatesRepository.addDayTypeAssignmentForDate(dayTypeId, date, currentEntity.id)
            } else {
                dayTypeData.excludedDates.add(date)
                activeDatesRepository.addDayTypeAssignmentForExcludedDate(dayTypeId, date, currentEntity.id)
            }
        }

        resetContext(context)
    }

    private fun resetContext(context: ActiveDatesParsingContext) {
        context.currentDayTypeAssignmentDayTypeRef = null
        context.currentDayTypeAssignmentOperatingDay = null
        context.currentDayTypeAssignmentOperatingPeriod = null
        context.currentDayTypeAssignmentDate = null
        context.currentDayTypeAssignmentIsAvailable = true
    }
}
