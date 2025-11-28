package org.entur.ror.ashur.sax.plugins.activedates.handlers

import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.ror.ashur.data.TestDataFactory
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesParsingContext
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesRepository
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDateTime
import kotlin.test.Test

class FromDateHandlerTest {
    val repo = ActiveDatesRepository()
    val context = ActiveDatesParsingContext()
    val handler = FromDateHandler(repo)
    val date = LocalDateTime.of(2026, 1, 1, 0, 0)
    val dateString = date.toString()
    val operatingPeriodId = "TST:OperatingPeriod:1"
    val entity = TestDataFactory.defaultEntity(
        id = operatingPeriodId,
        type = NetexTypes.OPERATING_PERIOD
    )

    @Test
    fun handlerCollectsFromDateForOperatingPeriod() {
        handler.characters(context, dateString.toCharArray(), 0, dateString.length)
        handler.endElement(context, entity)
        assertEquals(
            repo.getOperatingPeriodData(operatingPeriodId).period?.fromDate,
            date.toLocalDate()
        )

        // Verifies that FromDate context is reset correctly between handler runs
        val anotherDate = LocalDateTime.of(2027, 1, 1, 0, 0)
        val anotherDateString = anotherDate.toString()
        handler.characters(context, anotherDateString.toCharArray(), 0, anotherDateString.length)
        handler.endElement(context, entity)
        assertEquals(
            repo.getOperatingPeriodData(operatingPeriodId).period?.fromDate,
            anotherDate.toLocalDate()
        )
    }
}