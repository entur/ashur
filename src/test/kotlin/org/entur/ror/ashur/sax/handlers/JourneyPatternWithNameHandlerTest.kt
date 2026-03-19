package org.entur.ror.ashur.sax.handlers

import org.entur.netex.tools.lib.output.DelegatingXMLElementWriter
import org.entur.ror.ashur.sax.plugins.journeypatternname.JourneyPatternNameRepository
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.mock
import org.mockito.kotlin.*
import org.xml.sax.helpers.AttributesImpl
import kotlin.test.Test

class JourneyPatternWithNameHandlerTest {
    private val writer = mock<DelegatingXMLElementWriter>()

    @BeforeEach
    fun setUp() {
        reset(writer)
    }

    private fun attrsWithId(id: String): AttributesImpl {
        val attrs = AttributesImpl()
        attrs.addAttribute("", "id", "id", "CDATA", id)
        return attrs
    }

    @Test
    fun `adds Name element when JourneyPattern has no name and Route has name`() {
        val repo = JourneyPatternNameRepository()
        repo.routeNames["TST:Route:1"] = "Route Name From Route"
        repo.journeyPatternRouteRefs["TST:JourneyPattern:1"] = "TST:Route:1"

        val handler = JourneyPatternWithNameHandler(repo)
        val attrs = attrsWithId("TST:JourneyPattern:1")

        handler.startElement("", "JourneyPattern", "JourneyPattern", attrs, writer)

        inOrder(writer) {
            verify(writer).startElement("", "JourneyPattern", "JourneyPattern", attrs)
            verify(writer).startElement("", "Name", "Name", null)
            verify(writer).characters(eq("Route Name From Route".toCharArray()), eq(0), eq(21))
            verify(writer).endElement("", "Name", "Name")
        }
    }

    @Test
    fun `does not add Name element when JourneyPattern already has name`() {
        val repo = JourneyPatternNameRepository()
        repo.routeNames["TST:Route:1"] = "Route Name"
        repo.journeyPatternRouteRefs["TST:JourneyPattern:1"] = "TST:Route:1"
        repo.journeyPatternsWithName.add("TST:JourneyPattern:1")

        val handler = JourneyPatternWithNameHandler(repo)
        val attrs = attrsWithId("TST:JourneyPattern:1")

        handler.startElement("", "JourneyPattern", "JourneyPattern", attrs, writer)

        verify(writer).startElement("", "JourneyPattern", "JourneyPattern", attrs)
        verify(writer, never()).startElement(eq(""), eq("Name"), eq("Name"), isNull())
    }

    @Test
    fun `does not add Name element when Route has no name`() {
        val repo = JourneyPatternNameRepository()
        repo.journeyPatternRouteRefs["TST:JourneyPattern:1"] = "TST:Route:1"
        // Route has no name

        val handler = JourneyPatternWithNameHandler(repo)
        val attrs = attrsWithId("TST:JourneyPattern:1")

        handler.startElement("", "JourneyPattern", "JourneyPattern", attrs, writer)

        verify(writer).startElement("", "JourneyPattern", "JourneyPattern", attrs)
        verify(writer, never()).startElement(eq(""), eq("Name"), eq("Name"), isNull())
    }

    @Test
    fun `does not add Name element when JourneyPattern has no RouteRef`() {
        val repo = JourneyPatternNameRepository()
        repo.routeNames["TST:Route:1"] = "Route Name"
        // No RouteRef mapping for this JourneyPattern

        val handler = JourneyPatternWithNameHandler(repo)
        val attrs = attrsWithId("TST:JourneyPattern:1")

        handler.startElement("", "JourneyPattern", "JourneyPattern", attrs, writer)

        verify(writer).startElement("", "JourneyPattern", "JourneyPattern", attrs)
        verify(writer, never()).startElement(eq(""), eq("Name"), eq("Name"), isNull())
    }

    @Test
    fun `does not throw when JourneyPattern has no id attribute`() {
        val repo = JourneyPatternNameRepository()
        val handler = JourneyPatternWithNameHandler(repo)
        val attrs = AttributesImpl()

        handler.startElement("", "JourneyPattern", "JourneyPattern", attrs, writer)

        verify(writer).startElement(eq(""), eq("JourneyPattern"), eq("JourneyPattern"), eq(attrs))
        verify(writer, never()).startElement(eq(""), eq("Name"), eq("Name"), isNull())
    }

    @Test
    fun `passes through characters as-is`() {
        val repo = JourneyPatternNameRepository()
        val handler = JourneyPatternWithNameHandler(repo)
        val chars = "some content".toCharArray()

        handler.characters(chars, 0, chars.size, writer)

        verify(writer).characters(chars, 0, chars.size)
    }

    @Test
    fun `passes through end element as-is`() {
        val repo = JourneyPatternNameRepository()
        val handler = JourneyPatternWithNameHandler(repo)

        handler.endElement("", "JourneyPattern", "JourneyPattern", writer)

        verify(writer).endElement("", "JourneyPattern", "JourneyPattern")
    }
}
