package org.entur.ror.ashur.sax.handlers

import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.netex.tools.lib.output.DelegatingXMLElementWriter
import org.junit.Test
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify

class AvailabilityConditionHandlerTest {
    val codespace = "tst"
    val handler = AvailabilityConditionHandler(codespace = codespace)
    val writer = mock<DelegatingXMLElementWriter>()

    @BeforeEach
    fun setUp() {
        reset(writer)
    }

    @Test
    fun handlerShouldWriteFixedPeriodToAvailabilityCondition() {
        handler.startElement(
            "",
            NetexTypes.AVAILABILITY_CONDITION,
            NetexTypes.AVAILABILITY_CONDITION,
            null,
            writer
        )
        handler.characters("".toCharArray(), 0, 0, writer)
        handler.endElement("",
            NetexTypes.AVAILABILITY_CONDITION,
            NetexTypes.AVAILABILITY_CONDITION,
            writer
        )

        verify(writer).startElement("", NetexTypes.AVAILABILITY_CONDITION, NetexTypes.AVAILABILITY_CONDITION, null)
        verify(writer).startElement("", NetexTypes.FROM_DATE, NetexTypes.FROM_DATE, null)
        verify(writer).endElement("", NetexTypes.FROM_DATE, NetexTypes.FROM_DATE)
        verify(writer).startElement("", NetexTypes.TO_DATE, NetexTypes.TO_DATE, null)
        verify(writer).endElement("", NetexTypes.TO_DATE, NetexTypes.TO_DATE)
        verify(writer).endElement("", NetexTypes.AVAILABILITY_CONDITION, NetexTypes.AVAILABILITY_CONDITION)
    }

}