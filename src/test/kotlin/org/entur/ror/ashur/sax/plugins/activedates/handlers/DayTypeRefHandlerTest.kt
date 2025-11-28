package org.entur.ror.ashur.sax.plugins.activedates.handlers

import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.ror.ashur.data.TestDataFactory
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesParsingContext
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesRepository
import org.junit.jupiter.api.Assertions
import org.xml.sax.helpers.AttributesImpl
import kotlin.test.Test
import kotlin.test.assertEquals

class DayTypeRefHandlerTest {
    val context = ActiveDatesParsingContext()
    val repo = ActiveDatesRepository()
    val handler = DayTypeRefHandler(repo)
    val dayTypeRef = "TST:DayType:1"

    private fun attrs(): AttributesImpl {
        val attrs = AttributesImpl()
        attrs.addAttribute(
            "",
            "",
            "ref",
            NetexTypes.DAY_TYPE_REF,
            dayTypeRef
        )
        return attrs
    }

    @Test
    fun handlerCollectsDayTypeForDayTypeAssignment() {
        val entity = TestDataFactory.defaultEntity(
            id = "TST:DayTypeAssignment:1",
            type = NetexTypes.DAY_TYPE_ASSIGNMENT
        )
        handler.startElement(context, attrs(), entity)
        assertEquals(dayTypeRef, context.currentDayTypeAssignmentDayTypeRef)
    }

    @Test
    fun handlerCollectsDayTypeForServiceJourney() {
        val entity = TestDataFactory.defaultEntity(
            id = "TST:ServiceJourney:1",
            type = NetexTypes.SERVICE_JOURNEY
        )
        handler.startElement(context, attrs(), entity)
        Assertions.assertTrue(repo.getServiceJourneyData("TST:ServiceJourney:1").dayTypes.contains(dayTypeRef))
    }

    @Test
    fun handlerCollectsDayTypeForDeadRun() {
        val entity = TestDataFactory.defaultEntity(
            id = "TST:DeadRun:1",
            type = NetexTypes.DEAD_RUN
        )
        handler.startElement(context, attrs(), entity)
        Assertions.assertTrue(repo.getDeadRunData("TST:DeadRun:1").dayTypes.contains(dayTypeRef))
    }
}