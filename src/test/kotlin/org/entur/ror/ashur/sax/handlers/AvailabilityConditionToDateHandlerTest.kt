package org.entur.ror.ashur.sax.handlers

import org.entur.netex.tools.lib.output.DelegatingXMLElementWriter
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verifyNoInteractions

class AvailabilityConditionToDateHandlerTest {
    @Test
    fun availabilityConditionFromDateHandlerIsANoOp() {
        val writer = mock<DelegatingXMLElementWriter>()
        val handler = AvailabilityConditionFromDateHandler()
        handler.startElement("", "ToDate", "ToDate", null, mock())
        handler.characters("".toCharArray(), 0, 0, writer)
        handler.endElement("", "ToDate", "ToDate", writer)
        verifyNoInteractions(writer)
    }
}