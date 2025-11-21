package org.entur.ror.ashur.sax.handlers

import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.netex.tools.lib.output.DelegatingXMLElementWriter
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.mock
import org.mockito.kotlin.check
import org.mockito.kotlin.eq
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.xml.sax.helpers.AttributesImpl
import kotlin.test.Test
import kotlin.test.assertEquals

class ValidBetweenHandlerTest {
    val writer = mock<DelegatingXMLElementWriter>()

    @BeforeEach
    fun setUp() {
        reset(writer)
    }

    @Test
    fun testStartElementRewritesValidBetweenToAvailabilityCondition() {
        val handler = ValidBetweenHandler("tst")
        handler.startElement(
            "",
            "ValidBetween",
            "ValidBetween",
            AttributesImpl(),
            writer
        )

        verify(writer).startElement(
            eq(""),
            eq(NetexTypes.AVAILABILITY_CONDITION),
            eq(NetexTypes.AVAILABILITY_CONDITION),
            check {
                assertEquals("TST:AvailabilityCondition:1", it.getValue("id"))
                assertEquals("1", it.getValue("version"))
            }
        )
    }

    @Test
    fun testCharactersWritesAsIs() {
        val handler = ValidBetweenHandler("tst")
        handler.characters("".toCharArray(), 0, 0, writer)
        verify(writer).characters("".toCharArray(), 0, 0)
    }

    @Test
    fun testEndElementRewritesValidBetweenToAvailabilityCondition() {
        val handler = ValidBetweenHandler("tst")
        handler.endElement("", "ValidBetween", "ValidBetween", writer)
        verify(writer).endElement("", NetexTypes.AVAILABILITY_CONDITION, NetexTypes.AVAILABILITY_CONDITION)
    }
}