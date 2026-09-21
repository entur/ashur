package org.entur.ror.ashur.sax.handlers

import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.netex.tools.lib.output.DelegatingXMLElementWriter
import kotlin.test.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verifyNoInteractions

class AvailabilityConditionToDateHandlerTest {
    val elementName = NetexTypes.TO_DATE
    
    @Test
    fun availabilityConditionToDateHandlerIsANoOp() {
        val writer = mock<DelegatingXMLElementWriter>()
        val handler = AvailabilityConditionToDateHandler()
        handler.startElement("", elementName, elementName, null, mock())
        handler.characters("".toCharArray(), 0, 0, writer)
        handler.endElement("", elementName, elementName, writer)
        verifyNoInteractions(writer)
    }
}