package org.entur.ror.ashur.sax.plugins.activedates.handlers

import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.ror.ashur.data.TestDataFactory
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesParsingContext
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesRepository
import org.xml.sax.helpers.AttributesImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DeadRunHandlerTest {
    val context = ActiveDatesParsingContext()
    val repo = ActiveDatesRepository()
    val handler = DeadRunHandler(repo)
    val entity = TestDataFactory.defaultEntity(
        id = "TST:DeadRun:1",
        type = NetexTypes.DEAD_RUN
    )

    @Test
    fun handlerSetsCurrentDeadRunOnContext() {
        handler.startElement(context, AttributesImpl(), entity)
        assertEquals(context.currentDeadRunId, entity.id)
    }

    @Test
    fun handlerResetsCurrentDeadRunOnEndElement() {
        handler.startElement(context, AttributesImpl(), entity)
        handler.endElement(context, entity)
        assertNull(context.currentDeadRunId)
    }
}