package org.entur.ror.ashur.sax.plugins.activedates.handlers

import org.entur.netex.tools.lib.model.Entity
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesParsingContext
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesRepository
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesDataCollector
import java.time.LocalDate

class CalendarDateHandler(val activeDatesRepository: ActiveDatesRepository): ActiveDatesDataCollector() {
    private val stringBuilder = StringBuilder()

    override fun characters(context: ActiveDatesParsingContext, ch: CharArray?, start: Int, length: Int) {
        stringBuilder.append(ch, start, length)
    }

    override fun endElement(context: ActiveDatesParsingContext, currentEntity: Entity) {
        val calendarDate = LocalDate.parse(stringBuilder.trim().toString())
        activeDatesRepository.operatingDays[currentEntity.id] = calendarDate
        stringBuilder.setLength(0)
    }
}