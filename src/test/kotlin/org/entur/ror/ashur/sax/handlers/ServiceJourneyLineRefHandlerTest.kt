package org.entur.ror.ashur.sax.handlers

import org.entur.netex.tools.lib.output.DelegatingXMLElementWriter
import org.entur.ror.ashur.sax.plugins.operatorref.OperatorRefRepository
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.mock
import org.mockito.kotlin.*
import org.xml.sax.helpers.AttributesImpl
import kotlin.test.Test

class ServiceJourneyLineRefHandlerTest {
    private val writer = mock<DelegatingXMLElementWriter>()

    @BeforeEach
    fun setUp() {
        reset(writer)
    }

    private fun attrsWithRef(ref: String): AttributesImpl {
        val attrs = AttributesImpl()
        attrs.addAttribute("", "ref", "ref", "CDATA", ref)
        return attrs
    }

    @Test
    fun `injects OperatorRef before LineRef when ServiceJourney has no OperatorRef`() {
        val repository = OperatorRefRepository()
        repository.lineOperatorRefs["TST:Line:1"] = "TST:Operator:1"

        val context = ServiceJourneyOperatorRefContext()
        context.currentServiceJourneyId = "TST:ServiceJourney:1"

        val handler = ServiceJourneyLineRefHandler(context, repository)
        val attrs = attrsWithRef("TST:Line:1")

        handler.startElement("", "LineRef", "LineRef", attrs, writer)

        inOrder(writer) {
            verify(writer).startElement(eq(""), eq("OperatorRef"), eq("OperatorRef"), argThat<AttributesImpl> {
                getValue("ref") == "TST:Operator:1"
            })
            verify(writer).endElement("", "OperatorRef", "OperatorRef")
            verify(writer).startElement("", "LineRef", "LineRef", attrs)
        }
    }

    @Test
    fun `does not inject OperatorRef when ServiceJourney already has one`() {
        val repository = OperatorRefRepository()
        repository.lineOperatorRefs["TST:Line:1"] = "TST:Operator:1"
        repository.serviceJourneysWithOperatorRef.add("TST:ServiceJourney:1")

        val context = ServiceJourneyOperatorRefContext()
        context.currentServiceJourneyId = "TST:ServiceJourney:1"

        val handler = ServiceJourneyLineRefHandler(context, repository)
        val attrs = attrsWithRef("TST:Line:1")

        handler.startElement("", "LineRef", "LineRef", attrs, writer)

        verify(writer).startElement("", "LineRef", "LineRef", attrs)
        verify(writer, never()).startElement(eq(""), eq("OperatorRef"), eq("OperatorRef"), any())
    }

    @Test
    fun `does not inject OperatorRef when Line has no Operator`() {
        val repository = OperatorRefRepository()

        val context = ServiceJourneyOperatorRefContext()
        context.currentServiceJourneyId = "TST:ServiceJourney:1"

        val handler = ServiceJourneyLineRefHandler(context, repository)
        val attrs = attrsWithRef("TST:Line:1")

        handler.startElement("", "LineRef", "LineRef", attrs, writer)

        verify(writer).startElement("", "LineRef", "LineRef", attrs)
        verify(writer, never()).startElement(eq(""), eq("OperatorRef"), eq("OperatorRef"), any())
    }

    @Test
    fun `does not inject OperatorRef when context has no ServiceJourney id`() {
        val repository = OperatorRefRepository()
        repository.lineOperatorRefs["TST:Line:1"] = "TST:Operator:1"

        val context = ServiceJourneyOperatorRefContext()

        val handler = ServiceJourneyLineRefHandler(context, repository)
        val attrs = attrsWithRef("TST:Line:1")

        handler.startElement("", "LineRef", "LineRef", attrs, writer)

        verify(writer).startElement("", "LineRef", "LineRef", attrs)
        verify(writer, never()).startElement(eq(""), eq("OperatorRef"), eq("OperatorRef"), any())
    }

    @Test
    fun `passes through characters as-is`() {
        val repository = OperatorRefRepository()
        val context = ServiceJourneyOperatorRefContext()
        val handler = ServiceJourneyLineRefHandler(context, repository)
        val chars = "some content".toCharArray()

        handler.characters(chars, 0, chars.size, writer)

        verify(writer).characters(chars, 0, chars.size)
    }

    @Test
    fun `passes through end element as-is`() {
        val repository = OperatorRefRepository()
        val context = ServiceJourneyOperatorRefContext()
        val handler = ServiceJourneyLineRefHandler(context, repository)

        handler.endElement("", "LineRef", "LineRef", writer)

        verify(writer).endElement("", "LineRef", "LineRef")
    }
}
