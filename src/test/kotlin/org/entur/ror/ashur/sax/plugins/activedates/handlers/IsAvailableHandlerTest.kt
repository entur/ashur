package org.entur.ror.ashur.sax.plugins.activedates.handlers

import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.ror.ashur.data.TestDataFactory
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesParsingContext
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesRepository
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.xml.sax.helpers.AttributesImpl

class IsAvailableHandlerTest {
    private val repository = ActiveDatesRepository()
    private val handler = IsAvailableHandler(repository)

    private val dayTypeAssignment = TestDataFactory.defaultEntity(
        type = NetexTypes.DAY_TYPE_ASSIGNMENT,
        id = "TST:DayTypeAssignment:1"
    )

    private fun feed(context: ActiveDatesParsingContext, text: String) {
        handler.startElement(context, AttributesImpl(), dayTypeAssignment)
        handler.characters(context, text.toCharArray(), 0, text.length)
        handler.endElement(context, dayTypeAssignment)
    }

    @Test
    fun `false sets the flag to false`() {
        val context = ActiveDatesParsingContext()
        feed(context, "false")
        assertFalse(context.currentDayTypeAssignmentIsAvailable)
    }

    @Test
    fun `true sets the flag to true`() {
        val context = ActiveDatesParsingContext(currentDayTypeAssignmentIsAvailable = false)
        feed(context, "true")
        assertTrue(context.currentDayTypeAssignmentIsAvailable)
    }

    @Test
    fun `whitespace around the value is tolerated`() {
        val context = ActiveDatesParsingContext()
        feed(context, "  false\n")
        assertFalse(context.currentDayTypeAssignmentIsAvailable)
    }

    @Test
    fun `unknown text leaves the flag at its current value`() {
        val context = ActiveDatesParsingContext()
        feed(context, "maybe")
        assertTrue(context.currentDayTypeAssignmentIsAvailable)
    }

    @Test
    fun `non-DTA parent entity is ignored`() {
        val context = ActiveDatesParsingContext()
        val nonDtaEntity = TestDataFactory.defaultEntity(
            type = NetexTypes.DAY_TYPE,
            id = "TST:DayType:1"
        )
        handler.startElement(context, AttributesImpl(), nonDtaEntity)
        handler.characters(context, "false".toCharArray(), 0, 5)
        handler.endElement(context, nonDtaEntity)
        assertTrue(context.currentDayTypeAssignmentIsAvailable)
    }
}
