package org.entur.ror.ashur.sax.handlers

import org.entur.netex.tools.lib.output.DelegatingXMLElementWriter
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verifyNoInteractions

class AvailabilityConditionFromDateHandlerTest {
    @Test
    fun availabilityConditionFromDateHandlerIsANoOp() {
        val writer = mock<DelegatingXMLElementWriter>()
        val handler = AvailabilityConditionFromDateHandler()
        handler.startElement("", "FromDate", "FromDate", null, mock())
        handler.characters("".toCharArray(), 0, 0, writer)
        handler.endElement("", "FromDate", "FromDate", writer)
        verifyNoInteractions(writer)
    }
}