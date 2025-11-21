package org.entur.ror.ashur.sax.plugins.activedates.handlers

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesParsingContext
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesRepository
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesDataCollector
import org.entur.ror.ashur.sax.plugins.activedates.model.Period
import java.time.LocalDateTime

class ToDateHandler(
    val activeDatesRepository: ActiveDatesRepository
) : ActiveDatesDataCollector() {
    private val stringBuilder = StringBuilder()

    override fun characters(context: ActiveDatesParsingContext, ch: CharArray?, start: Int, length: Int) {
        stringBuilder.append(ch, start, length)
    }

    override fun endElement(context: ActiveDatesParsingContext, currentEntity: Entity) {
        if (currentEntity.type == NetexTypes.OPERATING_PERIOD) {
            val operatingPeriodId = currentEntity.id
            val opPeriodData = activeDatesRepository.getOperatingPeriodData(operatingPeriodId)
            val existingFromDate = opPeriodData.period?.fromDate
            opPeriodData.period = Period(
                fromDate = existingFromDate,
                toDate = LocalDateTime.parse(stringBuilder.toString()).toLocalDate(),
            )
        }
        stringBuilder.setLength(0)
    }
}