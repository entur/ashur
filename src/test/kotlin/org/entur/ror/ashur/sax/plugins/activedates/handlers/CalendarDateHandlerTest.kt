package org.entur.ror.ashur.sax.plugins.activedates.handlers

import org.entur.ror.ashur.data.TestDataFactory
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesParsingContext
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesRepository
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDate
import kotlin.test.Test

class CalendarDateHandlerTest {
    val context = ActiveDatesParsingContext()
    val activeDatesRepository = ActiveDatesRepository()

    val date1 = LocalDate.of(2026, 1, 1)
    val date2 = LocalDate.of(2026, 2, 2)

    val operatingDay1 = TestDataFactory.defaultEntity(type = "OperatingDay", id = "1")
    val operatingDay2 = TestDataFactory.defaultEntity(type = "OperatingDay", id = "2")

    @Test
    fun testCalendarDateIsConnectedToOperatingDayId() {
        val handler = CalendarDateHandler(activeDatesRepository)
        val date1CharArray = date1.toString().toCharArray()
        handler.characters(context, date1CharArray, 0, date1CharArray.size)
        handler.endElement(context, operatingDay1)
        assertEquals(activeDatesRepository.operatingDays[operatingDay1.id], date1)

        // Verifies that the internal stringBuilder is reset between handler's endElement calls
        val date2CharArray = date2.toString().toCharArray()
        handler.characters(context, date2CharArray, 0, date2CharArray.size)
        handler.endElement(context, operatingDay2)
    }
}