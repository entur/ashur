package org.entur.ror.ashur.sax.handlers

import org.entur.netex.tools.lib.output.DelegatingXMLElementWriter
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.mock
import org.mockito.kotlin.check
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.xml.sax.helpers.AttributesImpl
import kotlin.test.Test
import kotlin.test.assertEquals

class PublicationDeliveryHandlerTest {
    private val writer = mock<DelegatingXMLElementWriter>()
    private val handler = PublicationDeliveryHandler()

    @BeforeEach
    fun setUp() {
        reset(writer)
    }

    @Test
    fun testStartElementDeclaresNamespacePrefixesBeforeElement() {
        val attrs = AttributesImpl()
        attrs.addAttribute("", "version", "version", "CDATA", "old-version")
        attrs.addAttribute("", "xmlns:xsi", "xmlns:xsi", "CDATA", "http://www.w3.org/2001/XMLSchema-instance")

        handler.startElement("http://www.netex.org.uk/netex", "PublicationDelivery", "PublicationDelivery", attrs, writer)

        val inOrder = inOrder(writer)
        inOrder.verify(writer).startPrefixMapping("", "http://www.netex.org.uk/netex")
        inOrder.verify(writer).startPrefixMapping("gis", "http://www.opengis.net/gml/3.2")
        inOrder.verify(writer).startPrefixMapping("siri", "http://www.siri.org.uk/siri")
        inOrder.verify(writer).startElement(
            eq("http://www.netex.org.uk/netex"),
            eq("PublicationDelivery"),
            eq("PublicationDelivery"),
            check {
                assertEquals(1, it.length, "Should only have version attribute")
                assertEquals("1.15:NO-NeTEx-networktimetable:1.5", it.getValue("version"))
            }
        )
    }

    @Test
    fun testStartElementReplacesAllAttributesWithStandardVersion() {
        val attrs = AttributesImpl()
        attrs.addAttribute("", "version", "version", "CDATA", "some-other-version")
        attrs.addAttribute("", "custom", "custom", "CDATA", "custom-value")

        handler.startElement("", "PublicationDelivery", "PublicationDelivery", attrs, writer)

        verify(writer).startElement(
            eq(""),
            eq("PublicationDelivery"),
            eq("PublicationDelivery"),
            check {
                assertEquals(1, it.length, "Should only have version attribute")
                assertEquals("1.15:NO-NeTEx-networktimetable:1.5", it.getValue("version"))
            }
        )
    }

    @Test
    fun testCharactersIsWrittenAsIs() {
        val ch = "some content".toCharArray()
        handler.characters(ch, 0, ch.size, writer)
        verify(writer).characters(ch, 0, ch.size)
    }

    @Test
    fun testEndElementEndsNamespacesInReverseOrder() {
        handler.endElement("http://www.netex.org.uk/netex", "PublicationDelivery", "PublicationDelivery", writer)

        val inOrder = inOrder(writer)
        inOrder.verify(writer).endElement("http://www.netex.org.uk/netex", "PublicationDelivery", "PublicationDelivery")
        inOrder.verify(writer).endPrefixMapping("siri")
        inOrder.verify(writer).endPrefixMapping("gis")
        inOrder.verify(writer).endPrefixMapping("")
    }
}
