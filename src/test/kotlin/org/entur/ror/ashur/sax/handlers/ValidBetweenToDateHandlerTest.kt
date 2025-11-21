package org.entur.ror.ashur.sax.handlers

import org.entur.netex.tools.lib.extensions.toISO8601
import org.entur.netex.tools.lib.output.DelegatingXMLElementWriter
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.mock
import org.mockito.kotlin.eq
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.xml.sax.helpers.AttributesImpl
import java.time.LocalDate
import kotlin.test.Test

class ValidBetweenToDateHandlerTest {
    val writer = mock<DelegatingXMLElementWriter>()

    @BeforeEach
    fun setUp() {
        reset(writer)
    }

    @Test
    fun testValidBetweenToDateStartElementWritesAsIs() {
        val handler = ValidBetweenToDateHandler(toDate = LocalDate.of(2026, 1, 1))
        val attrs = AttributesImpl()
        handler.startElement("", "ToDate", "ToDate", attrs, writer)
        verify(writer).startElement(eq(""), eq("ToDate"), eq("ToDate"), eq(attrs))
    }

    @Test
    fun testValidBetweenToDateCharactersRewritesDate() {
        val originalDate = LocalDate.of(2025, 2, 2).toString()
        val rewrittenDate = LocalDate.of(2026, 1, 1)

        val handler = ValidBetweenToDateHandler(toDate = rewrittenDate)
        handler.characters(originalDate.toCharArray(), 0, originalDate.length, writer)
        val rewrittenDateString = rewrittenDate.toISO8601()
        verify(writer).characters(rewrittenDateString.toCharArray(), 0, rewrittenDateString.length)
    }

    @Test
    fun testValidBetweenToDateEndElementWritesAsIs() {
        val handler = ValidBetweenToDateHandler(toDate = LocalDate.of(2026, 1, 1))
        handler.endElement("", "ToDate", "ToDate", writer)
        verify(writer).endElement(eq(""), eq("ToDate"), eq("ToDate"))
    }
}