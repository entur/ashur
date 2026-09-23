package org.entur.ror.ashur.sax.handlers

import org.entur.netex.tools.lib.extensions.toISO8601
import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.netex.tools.lib.output.DelegatingXMLElementWriter
import kotlin.test.Test
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.mock
import org.mockito.kotlin.check
import org.mockito.kotlin.eq
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.xml.sax.helpers.AttributesImpl
import java.time.LocalDate
import kotlin.test.assertEquals

class AvailabilityConditionHandlerTest {
    val availabilityConditionId = "TST:AvailabilityCondition:1234"
    val availabilityConditionVersion = "1"

    val fromDate = LocalDate.of(2027, 1, 1)
    val fromDateInXML = fromDate.toISO8601()

    val handler = AvailabilityConditionHandler(fromDate)

    val writer = mock<DelegatingXMLElementWriter>()

    @BeforeEach
    fun setUp() {
        reset(writer)
    }

    fun handleAvailabilityConditionEvents() {
        val attributes = AttributesImpl()
        attributes.addAttribute("", "version", "version", "", availabilityConditionVersion)
        attributes.addAttribute("", "id", "id", "", availabilityConditionId)

        handler.startElement(
            "",
            NetexTypes.AVAILABILITY_CONDITION,
            NetexTypes.AVAILABILITY_CONDITION,
            attributes,
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
                assertEquals(availabilityConditionId, it.getValue("id"))
                assertEquals(availabilityConditionVersion, it.getValue("version"))
            }
        )

        // verify FromDate element
        verify(writer).startElement("", NetexTypes.FROM_DATE, NetexTypes.FROM_DATE, null)
        verify(writer).characters(fromDateInXML.toCharArray(), 0, fromDateInXML.length)
        verify(writer).endElement("", NetexTypes.FROM_DATE, NetexTypes.FROM_DATE)

        // Verify AvailabilityCondition closing tag
        verify(writer).endElement("", NetexTypes.AVAILABILITY_CONDITION, NetexTypes.AVAILABILITY_CONDITION)
    }

}