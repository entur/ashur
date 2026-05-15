package org.entur.ror.ashur.sax.plugins.activedates.handlers

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesDataCollector
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesParsingContext
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesRepository
import org.xml.sax.Attributes

class IsAvailableHandler(val activeDatesRepository: ActiveDatesRepository) : ActiveDatesDataCollector() {

    private val stringBuilder = StringBuilder()

    override fun startElement(context: ActiveDatesParsingContext, attributes: Attributes?, currentEntity: Entity) {
        stringBuilder.setLength(0)
    }

    override fun characters(context: ActiveDatesParsingContext, ch: CharArray?, start: Int, length: Int) {
        stringBuilder.append(ch, start, length)
    }

    override fun endElement(context: ActiveDatesParsingContext, currentEntity: Entity) {
        if (currentEntity.type != NetexTypes.DAY_TYPE_ASSIGNMENT) return
        when (stringBuilder.trim().toString().lowercase()) {
            "false" -> context.currentDayTypeAssignmentIsAvailable = false
            "true" -> context.currentDayTypeAssignmentIsAvailable = true
        }
    }
}
