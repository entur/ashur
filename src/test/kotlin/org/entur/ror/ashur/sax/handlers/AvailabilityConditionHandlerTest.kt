package org.entur.ror.ashur.sax.handlers

import org.entur.netex.tools.lib.extensions.toISO8601
import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.netex.tools.lib.output.DelegatingXMLElementWriter
import org.junit.Test
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.mock
import org.mockito.kotlin.check
import org.mockito.kotlin.eq
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import java.time.LocalDate
import kotlin.test.assertEquals

class AvailabilityConditionHandlerTest {
    val codespace = "tst"

    val fromDate = LocalDate.of(2027, 1, 1)
    val fromDateInXML = fromDate.toISO8601()

    val toDate = LocalDate.of(2027, 1, 31)
    val toDateInXML = toDate.toISO8601()

    val handler = AvailabilityConditionHandler(
        codespace = codespace,
        fromDate = fromDate,
        toDate = toDate,
    )

    val writer = mock<DelegatingXMLElementWriter>()

    @BeforeEach
    fun setUp() {
        reset(writer)
    }

    fun handleAvailabilityConditionEvents() {
        handler.startElement(
            "",
            NetexTypes.AVAILABILITY_CONDITION,
            NetexTypes.AVAILABILITY_CONDITION,
            null,
            writer
        )
        handler.characters("".toCharArray(), 0, 0, writer)
        handler.endElement(
            "",
            NetexTypes.AVAILABILITY_CONDITION,
            NetexTypes.AVAILABILITY_CONDITION,
            writer
        )
    }

    @Test
    fun handlerShouldWriteFixedPeriodToAvailabilityCondition() {
        handleAvailabilityConditionEvents()

        // verify AvailabilityCondition starting tag
        verify(writer).startElement(
            eq(""),
            eq(NetexTypes.AVAILABILITY_CONDITION),
            eq(NetexTypes.AVAILABILITY_CONDITION),
            check {
                assertEquals("TST:AvailabilityCondition:1", it.getValue("id"))
                assertEquals("1", it.getValue("version"))
            }
        )

        // verify FromDate element
        verify(writer).startElement("", NetexTypes.FROM_DATE, NetexTypes.FROM_DATE, null)
        verify(writer).characters(fromDateInXML.toCharArray(), 0, fromDateInXML.length)
        verify(writer).endElement("", NetexTypes.FROM_DATE, NetexTypes.FROM_DATE)

        // Verify ToDate element
        verify(writer).startElement("", NetexTypes.TO_DATE, NetexTypes.TO_DATE, null)
        verify(writer).characters(toDateInXML.toCharArray(), 0, toDateInXML.length)
        verify(writer).endElement("", NetexTypes.TO_DATE, NetexTypes.TO_DATE)

        // Verify AvailabilityCondition closing tag
        verify(writer).endElement("", NetexTypes.AVAILABILITY_CONDITION, NetexTypes.AVAILABILITY_CONDITION)
    }

}