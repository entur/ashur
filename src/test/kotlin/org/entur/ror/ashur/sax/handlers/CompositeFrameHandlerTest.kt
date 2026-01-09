package org.entur.ror.ashur.sax.handlers

import org.entur.netex.tools.lib.output.DelegatingXMLElementWriter
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.mock
import org.mockito.kotlin.check
import org.mockito.kotlin.eq
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.xml.sax.helpers.AttributesImpl
import java.time.LocalDateTime
import java.time.Month
import kotlin.test.Test
import kotlin.test.assertEquals

class CompositeFrameHandlerTest {
    val writer = mock<DelegatingXMLElementWriter>()

    @BeforeEach
    fun setUp() {
        reset(writer)
    }

    @Test
    fun testStartElementAddsCreatedTimestampIfProvided() {
        val timestamp = LocalDateTime.of(2026, Month.JANUARY, 1, 0, 0, 0)
        val handler = CompositeFrameHandler(timestamp)
        handler.startElement("", "", "", AttributesImpl(), writer)
        verify(writer).startElement(
            eq(""),
            eq(""),
            eq(""),
            check {
                val createdTimestampBeingWritten = it.getValue("created")
                assertEquals(timestamp.toString(), createdTimestampBeingWritten)
            }
        )
    }

    @Test
    fun testStartElementLeavesCreatedTimestampAsIsIfNotProvided() {
        val handler = CompositeFrameHandler(null)
        val attrs = AttributesImpl()
        handler.startElement("", "", "", attrs, writer)
        verify(writer).startElement("", "", "", attrs)

        verify(writer).startElement("", "", "FrameDefaults", null)
        verify(writer).startElement("", "", "DefaultLocale", null)

        verify(writer).startElement("", "", "TimeZone", null)
        verify(writer).characters("CET".toCharArray(), 0, "CET".length)
        verify(writer).endElement("", "", "TimeZone")

        verify(writer).startElement("", "", "DefaultLanguage", null)
        verify(writer).characters("no".toCharArray(), 0, "no".length)
        verify(writer).endElement("", "", "DefaultLanguage")

        verify(writer).endElement("", "", "DefaultLocale")
        verify(writer).endElement("", "", "FrameDefaults")
    }

    @Test
    fun testCharactersIsWrittenAsIs() {
        val handler = CompositeFrameHandler(null)
        val ch = "".toCharArray()
        handler.characters(ch, 0, 0, writer)
        verify(writer).characters(ch, 0, 0)
    }

    @Test
    fun testEndElementIsWrittenAsIs() {
        val handler = CompositeFrameHandler(null)
        handler.endElement("", "", "", writer)
        verify(writer).endElement("", "", "")
    }
}