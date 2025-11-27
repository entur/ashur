package org.entur.ror.ashur.sax.plugins.activedates.handlers

import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.ror.ashur.data.TestDataFactory
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesParsingContext
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesRepository
import org.xml.sax.helpers.AttributesImpl
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class DateHandlerTest {
    val repository = ActiveDatesRepository()
    val context = ActiveDatesParsingContext()
    val dateHandler = DateHandler(repository)

    val testDate = LocalDate.of(2026, 1, 1)
    val testDateString = testDate.toString()

    val dayTypeAssignment = TestDataFactory.defaultEntity(
        type = NetexTypes.DAY_TYPE_ASSIGNMENT,
        id = "TST:DayTypeAssignment:1"
    )

    @Test
    fun testDateHandlerCollectsDateForDayTypeAssignments() {
        dateHandler.characters(
            context,
            testDateString.toCharArray(),
            0,
            testDateString.length,
        )
        dateHandler.endElement(
            context,
            dayTypeAssignment,
        )
        assertEquals(testDate, context.currentDayTypeAssignmentDate)

        // Verifies that Dates are collected correctly when there are multiple DayTypeAssignments
        dateHandler.startElement(context, AttributesImpl(), dayTypeAssignment)
        val anotherDate = LocalDate.of(2027, 1, 1)
        val anotherDateString = anotherDate.toString()
        dateHandler.characters(
            context,
            anotherDateString.toCharArray(),
            0,
            anotherDateString.length,
        )
        dateHandler.endElement(
            context,
            dayTypeAssignment,
        )
        assertEquals(anotherDate, context.currentDayTypeAssignmentDate)
    }
}