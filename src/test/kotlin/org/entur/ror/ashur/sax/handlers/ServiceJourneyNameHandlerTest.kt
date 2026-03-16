package org.entur.ror.ashur.sax.handlers

import org.entur.netex.tools.lib.output.DelegatingXMLElementWriter
import org.entur.ror.ashur.sax.plugins.servicejourneynaming.ServiceJourneyNameRepository
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.xml.sax.Attributes
import org.mockito.kotlin.whenever

class ServiceJourneyNameHandlerTest {

    @Test
    fun `injects Name element when FrontText is available and ServiceJourney has no existing Name`() {
        val repository = ServiceJourneyNameRepository()
        repository.destinationDisplayFrontText["DD:1"] = "Oslo S"
        repository.journeyPatternToDestinationDisplay["JP:1"] = "DD:1"
        repository.serviceJourneyToJourneyPattern["SJ:1"] = "JP:1"

        val handler = ServiceJourneyNameHandler(repository)
        val writer = mock<DelegatingXMLElementWriter>()
        val attributes = mock<Attributes>()
        whenever(attributes.getValue("id")).thenReturn("SJ:1")

        handler.startElement("", "ServiceJourney", "ServiceJourney", attributes, writer)

        // Verify start element was called
        verify(writer).startElement(eq(""), eq("ServiceJourney"), eq("ServiceJourney"), eq(attributes))

        // Verify Name element was injected
        verify(writer).startElement(eq(""), eq(""), eq("Name"), eq(null))
        verify(writer).characters(eq("Oslo S".toCharArray()), eq(0), eq(6))
        verify(writer).endElement(eq(""), eq(""), eq("Name"))
    }

    @Test
    fun `does not inject Name when ServiceJourney already has Name`() {
        val repository = ServiceJourneyNameRepository()
        repository.destinationDisplayFrontText["DD:1"] = "Oslo S"
        repository.journeyPatternToDestinationDisplay["JP:1"] = "DD:1"
        repository.serviceJourneyToJourneyPattern["SJ:1"] = "JP:1"
        repository.serviceJourneysWithName.add("SJ:1")

        val handler = ServiceJourneyNameHandler(repository)
        val writer = mock<DelegatingXMLElementWriter>()
        val attributes = mock<Attributes>()
        whenever(attributes.getValue("id")).thenReturn("SJ:1")

        handler.startElement("", "ServiceJourney", "ServiceJourney", attributes, writer)

        // Verify start element was called
        verify(writer).startElement(eq(""), eq("ServiceJourney"), eq("ServiceJourney"), eq(attributes))

        // Verify Name element was NOT injected
        verify(writer, never()).startElement(eq(""), eq(""), eq("Name"), any())
    }

    @Test
    fun `does not inject Name when FrontText cannot be resolved`() {
        val repository = ServiceJourneyNameRepository()
        // No data in repository - chain cannot be resolved

        val handler = ServiceJourneyNameHandler(repository)
        val writer = mock<DelegatingXMLElementWriter>()
        val attributes = mock<Attributes>()
        whenever(attributes.getValue("id")).thenReturn("SJ:1")

        handler.startElement("", "ServiceJourney", "ServiceJourney", attributes, writer)

        // Verify start element was called
        verify(writer).startElement(eq(""), eq("ServiceJourney"), eq("ServiceJourney"), eq(attributes))

        // Verify Name element was NOT injected
        verify(writer, never()).startElement(eq(""), eq(""), eq("Name"), any())
    }

    @Test
    fun `passes through characters`() {
        val repository = ServiceJourneyNameRepository()
        val handler = ServiceJourneyNameHandler(repository)
        val writer = mock<DelegatingXMLElementWriter>()

        val chars = "test content".toCharArray()
        handler.characters(chars, 0, chars.size, writer)

        verify(writer).characters(chars, 0, chars.size)
    }

    @Test
    fun `passes through endElement`() {
        val repository = ServiceJourneyNameRepository()
        val handler = ServiceJourneyNameHandler(repository)
        val writer = mock<DelegatingXMLElementWriter>()

        handler.endElement("", "ServiceJourney", "ServiceJourney", writer)

        verify(writer).endElement("", "ServiceJourney", "ServiceJourney")
    }
}
