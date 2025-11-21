package org.entur.ror.ashur.sax.handlers

import org.entur.netex.tools.lib.extensions.toAttributes
import org.entur.netex.tools.lib.output.DelegatingXMLElementWriter
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertNull
import org.mockito.Mockito.mock
import org.mockito.kotlin.check
import org.mockito.kotlin.eq
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import kotlin.test.Test
import kotlin.test.assertEquals

class QuayRefHandlerTest {
    val writer = mock<DelegatingXMLElementWriter>()

    @BeforeEach
    fun setUp() {
        reset(writer)
    }

    @Test
    fun testQuayRefStartElementKeepsOnlyRefAttributes() {
        val handler = QuayRefHandler()
        val attributes = mapOf(
            "ref" to "QuayRef:1",
            "version" to "1.0.0",
        ).toAttributes()

        handler.startElement("", "QuayRef", "QuayRef", attributes, writer)
        verify(writer).startElement(
            eq(""),
            eq("QuayRef"),
            eq("QuayRef"),
            check {
                assertEquals(it.getValue("ref"), attributes.getValue("ref"))
                assertNull(it.getValue("version"))
            }
        )
    }

    @Test
    fun testQuayRefCharactersWritesAsIs() {
        val handler = QuayRefHandler()
        handler.characters("".toCharArray(), 0, 0, writer)
        verify(writer).characters("".toCharArray(), 0, 0)
    }

    @Test
    fun testQuayRefEndElementWritesAsIs() {
        val handler = QuayRefHandler()
        handler.endElement("", "QuayRef", "QuayRef", writer)
        verify(writer).endElement("", "QuayRef", "QuayRef")
    }
}