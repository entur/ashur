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

class ValidBetweenFromDateHandlerTest {
    val writer = mock<DelegatingXMLElementWriter>()

    @BeforeEach
    fun setUp() {
        reset(writer)
    }

    @Test
    fun testValidBetweenFromDateStartElementWritesAsIs() {
        val handler = ValidBetweenFromDateHandler(fromDate = LocalDate.of(2026, 1, 1))
        val attrs = AttributesImpl()
        handler.startElement("", "FromDate", "FromDate", attrs, writer)
        verify(writer).startElement(eq(""), eq("FromDate"), eq("FromDate"), eq(attrs))
    }

    @Test
    fun testValidBetweenFromDateCharactersRewritesDate() {
        val originalDate = LocalDate.of(2025, 2, 2).toString()
        val rewrittenDate = LocalDate.of(2026, 1, 1)

        val handler = ValidBetweenFromDateHandler(fromDate = rewrittenDate)
        handler.characters(originalDate.toCharArray(), 0, originalDate.length, writer)
        val rewrittenDateString = rewrittenDate.toISO8601()
        verify(writer).characters(rewrittenDateString.toCharArray(), 0, rewrittenDateString.length)
    }

    @Test
    fun testValidBetweenFromDateEndElementWritesAsIs() {
        val handler = ValidBetweenFromDateHandler(fromDate = LocalDate.of(2026, 1, 1))
        handler.endElement("", "FromDate", "FromDate", writer)
        verify(writer).endElement(eq(""), eq("FromDate"), eq("FromDate"))
    }
}