package org.entur.ror.ashur.sax.handlers

import org.entur.netex.tools.lib.output.DelegatingXMLElementWriter
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.xml.sax.helpers.AttributesImpl

class DefaultLocaleHandlerTest {
    val writer = mock<DelegatingXMLElementWriter>()

    @BeforeEach
    fun setUp() {
        reset(writer)
    }

    @Test
    fun defaultLocaleHandlerStartElementWritesTimezoneAndDefaultLanguage() {
        val handler = DefaultLocaleHandler()
        val defaultLocaleAttrs = AttributesImpl()
        handler.startElement("", "DefaultLocale", "DefaultLocale", defaultLocaleAttrs, writer)
        verify(writer).startElement("", "DefaultLocale", "DefaultLocale", defaultLocaleAttrs)

        verify(writer).startElement(eq(""), eq("TimeZone"), eq("TimeZone"), any())
        verify(writer).characters("Europe/Oslo".toCharArray(), 0, "Europe/Oslo".length)
        verify(writer).endElement("", "TimeZone", "TimeZone")

        verify(writer).startElement(eq(""), eq("DefaultLanguage"), eq("DefaultLanguage"), any())
        verify(writer).characters("no".toCharArray(), 0, "no".length)
        verify(writer).endElement("", "DefaultLanguage", "DefaultLanguage")
    }

    @Test
    fun defaultLocaleCharactersWritesCharactersAsIs() {
        val handler = DefaultLocaleHandler()
        handler.characters("".toCharArray(), 0, 0, writer)
        verify(writer).characters("".toCharArray(), 0, 0)
    }

    @Test
    fun defaultLocaleEndElementWritesEndElementAsIs() {
        val handler = DefaultLocaleHandler()
        handler.endElement("", "DefaultLocale", "DefaultLocale", writer)
        verify(writer).endElement("", "DefaultLocale", "DefaultLocale")
    }
}