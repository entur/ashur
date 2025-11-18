package org.entur.ror.ashur.sax.plugins.activedates.handlers

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesParsingContext
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesRepository
import org.entur.ror.ashur.sax.plugins.activedates.NetexDataCollector
import org.xml.sax.Attributes

class OperatingPeriodRefHandler(val activeDatesRepository: ActiveDatesRepository) : NetexDataCollector() {
    override fun startElement(
        context: ActiveDatesParsingContext,
        attributes: Attributes?,
        currentEntity: Entity
    ) {
        if (currentEntity.type == NetexTypes.DAY_TYPE_ASSIGNMENT) {
            val ref = attributes?.getValue("ref")
            if (ref != null) {
                context.currentDayTypeAssignmentOperatingPeriod = ref
            }
        }
    }
}