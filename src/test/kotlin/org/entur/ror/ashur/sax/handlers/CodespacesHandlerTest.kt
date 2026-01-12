package org.entur.ror.ashur.sax.handlers

import org.entur.netex.tools.lib.output.DelegatingXMLElementWriter
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify

class CodespacesHandlerTest {
    val writer = mock<DelegatingXMLElementWriter>()

    @Test
    fun testStartElementWritesStartTagAsIs() {
        val handler = CodespacesHandler()
        val attrs = org.xml.sax.helpers.AttributesImpl()
        handler.startElement("", "", "codespaces", attrs, writer)
        verify(writer).startElement("", "", "codespaces", attrs)
    }

    @Test
    fun testCharactersWritesCharactersAsIs() {
        val handler = CodespacesHandler()
        val ch = "some characters".toCharArray()
        handler.characters(ch, 0, ch.size, writer)
        verify(writer).characters(ch, 0, ch.size)
    }

    @Test
    fun testEndElementWritesEndTagAndFrameDefaults() {
        val handler = CodespacesHandler()
        handler.endElement("", "", "codespaces", writer)

        verify(writer).startElement("", "", "FrameDefaults", null)
        verify(writer).startElement("", "", "DefaultLocale", null)

        verify(writer).startElement("", "", "TimeZone", null)
        verify(writer).characters("Europe/Oslo".toCharArray(), 0, "Europe/Oslo".length)
        verify(writer).endElement("", "", "TimeZone")

        verify(writer).startElement("", "", "DefaultLanguage", null)
        verify(writer).characters("no".toCharArray(), 0, "no".length)
        verify(writer).endElement("", "", "DefaultLanguage")

        verify(writer).endElement("", "", "DefaultLocale")
        verify(writer).endElement("", "", "FrameDefaults")
    }
}