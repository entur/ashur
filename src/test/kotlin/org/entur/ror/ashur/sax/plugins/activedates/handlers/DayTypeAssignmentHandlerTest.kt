package org.entur.ror.ashur.sax.plugins.activedates.handlers

import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.ror.ashur.data.TestDataFactory
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesParsingContext
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertNull
import java.time.LocalDate
import kotlin.test.Test

class DayTypeAssignmentHandlerTest {
    val context = ActiveDatesParsingContext()
    val repository = ActiveDatesRepository()
    val handler = DayTypeAssignmentHandler(repository)
    val dayTypeAssignmentId = "TST:DayTypeAssignment:1"
    val dayTypeAssignmentEntity = TestDataFactory.defaultEntity(
        type = NetexTypes.DAY_TYPE_ASSIGNMENT,
        id = dayTypeAssignmentId
    )

    @BeforeEach
    fun setUp() {
        context.currentDayTypeAssignmentDayTypeRef = dayTypeAssignmentEntity.id
    }

    @Test
    fun testHandlingOfDayTypeAssignmentWithOperatingDay() {
        val operatingDayId = "TST:OperatingDay:1"
        context.currentDayTypeAssignmentOperatingDay = operatingDayId
        handler.endElement(context, dayTypeAssignmentEntity)
        Assertions.assertTrue(
            repository
                .getDayTypeData(dayTypeAssignmentId)
                .operatingDays
                .contains(operatingDayId)
        )
        assertNull(context.currentDayTypeAssignmentOperatingDay)
    }

    @Test
    fun testHandlingOfDayTypeAssignmentWithOperatingPeriod() {
        val operatingPeriodId = "TST:OperatingPeriod:1"
        context.currentDayTypeAssignmentOperatingPeriod = operatingPeriodId
        handler.endElement(context, dayTypeAssignmentEntity)
        Assertions.assertTrue(
            repository
                .getDayTypeData(dayTypeAssignmentId)
                .operatingPeriods
                .contains(operatingPeriodId)
        )
        assertNull(context.currentDayTypeAssignmentOperatingPeriod)
    }

    @Test
    fun testHandlingOfDayTypeAssignmentWithDate() {
        val date = LocalDate.of(2026, 1, 1)
        context.currentDayTypeAssignmentDate = date
        handler.endElement(context, dayTypeAssignmentEntity)
        Assertions.assertTrue(
            repository
                .getDayTypeData(dayTypeAssignmentId)
                .dates
                .contains(date)
        )
        assertNull(context.currentDayTypeAssignmentDate)
    }
}