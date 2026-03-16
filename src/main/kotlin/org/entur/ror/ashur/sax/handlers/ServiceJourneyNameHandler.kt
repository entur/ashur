package org.entur.ror.ashur.sax.handlers

import org.entur.netex.tools.lib.output.DelegatingXMLElementWriter
import org.entur.netex.tools.lib.output.XMLElementHandler
import org.entur.ror.ashur.sax.plugins.servicejourneynaming.ServiceJourneyNameRepository
import org.xml.sax.Attributes

/**
 * Handler that injects a Name element as the first child of ServiceJourney.
 * The Name value is derived from DestinationDisplay.FrontText via the collected data.
 *
 * The injection only happens if:
 * - The ServiceJourney doesn't already have a Name element
 * - The data chain can be resolved (ServiceJourney -> JourneyPattern -> DestinationDisplay -> FrontText)
 *
 * Follows the pattern of CodespacesHandler for element injection.
 */
class ServiceJourneyNameHandler(
    private val repository: ServiceJourneyNameRepository
) : XMLElementHandler {

    override fun startElement(
        uri: String?,
        localName: String?,
        qName: String?,
        attributes: Attributes?,
        writer: DelegatingXMLElementWriter
    ) {
        // Write the ServiceJourney start tag
        writer.startElement(uri, localName, qName, attributes)

        // Extract the ServiceJourney ID and inject Name if applicable
        val serviceJourneyId = attributes?.getValue("id")

        if (serviceJourneyId != null && !repository.hasExistingName(serviceJourneyId)) {
            val frontText = repository.getFrontTextForServiceJourney(serviceJourneyId)
            if (frontText != null) {
                // Inject Name element immediately after start tag (as first child)
                writer.startElement("", "", "Name", null)
                writer.characters(frontText.toCharArray(), 0, frontText.length)
                writer.endElement("", "", "Name")
            }
        }
    }

    override fun characters(
        ch: CharArray?,
        start: Int,
        length: Int,
        writer: DelegatingXMLElementWriter
    ) {
        writer.characters(ch, start, length)
    }

    override fun endElement(
        uri: String?,
        localName: String?,
        qName: String?,
        writer: DelegatingXMLElementWriter
    ) {
        writer.endElement(uri, localName, qName)
    }
}
