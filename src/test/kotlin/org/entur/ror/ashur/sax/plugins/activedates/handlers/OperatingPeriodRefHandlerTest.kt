package org.entur.ror.ashur.sax.plugins.activedates.handlers

import org.entur.netex.tools.lib.extensions.toAttributes
import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.ror.ashur.data.TestDataFactory
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesParsingContext
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesRepository
import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

class OperatingPeriodRefHandlerTest {
    private val context = ActiveDatesParsingContext()
    private val repo = ActiveDatesRepository()
    private val handler = OperatingPeriodRefHandler(repo)

    @Test
    fun testOperatingPeriodRefIsCollectedForDayTypeAssignment() {
        val operatingPeriodRef = "TST:OperatingPeriod:1"

        handler.startElement(
            context,
            mapOf("ref" to operatingPeriodRef).toAttributes(),
            TestDataFactory.defaultEntity(
                id = "TST:DayTypeAssignment:1",
                type = NetexTypes.DAY_TYPE_ASSIGNMENT
            )
        )

        assertEquals(
            operatingPeriodRef,
            context.currentDayTypeAssignmentOperatingPeriod
        )
    }

}