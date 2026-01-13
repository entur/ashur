package org.entur.ror.ashur.sax.plugins.activedates.handlers

import org.entur.netex.tools.lib.extensions.toAttributes
import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.ror.ashur.data.TestDataFactory
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesParsingContext
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class OperatingDayRefHandlerTest {
    private val context = ActiveDatesParsingContext()
    private val repository = ActiveDatesRepository()
    private val handler = OperatingDayRefHandler(repository)
    private val operatingDayEntity = TestDataFactory.defaultEntity(
        id = "TST:OperatingDay:1",
        type = NetexTypes.OPERATING_DAY
    )

    @Test
    fun testOperatingDayAsPartOfDayTypeAssignment() {
        val dayTypeAssignmentEntity = TestDataFactory.defaultEntity(
            id = "TST:DayTypeAssignment:1",
            type = NetexTypes.DAY_TYPE_ASSIGNMENT
        )

        handler.startElement(
            context = context,
            attributes = mapOf("ref" to operatingDayEntity.id).toAttributes(),
            currentEntity = dayTypeAssignmentEntity
        )

        assertEquals(operatingDayEntity.id, context.currentDayTypeAssignmentOperatingDay)
    }

    @Test
    fun testOperatingDayAsPartOfDatedServiceJourney() {
        val datedServiceJourneyEntity = TestDataFactory.defaultEntity(
            id = "TST:DSJ:1",
            type = NetexTypes.DATED_SERVICE_JOURNEY
        )

        handler.startElement(
            context = context,
            attributes = mapOf("ref" to operatingDayEntity.id).toAttributes(),
            currentEntity = datedServiceJourneyEntity
        )

        assertEquals(operatingDayEntity.id, context.currentOperatingDayRef)
    }
}