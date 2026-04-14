package org.entur.ror.ashur.sax.handlers

import org.entur.netex.tools.lib.output.DelegatingXMLElementWriter
import org.entur.netex.tools.lib.output.XMLElementHandler
import org.entur.ror.ashur.sax.plugins.journeypatternname.JourneyPatternNameRepository
import org.xml.sax.Attributes

class JourneyPatternWithNameHandler(
    private val repository: JourneyPatternNameRepository
) : XMLElementHandler {

    override fun startElement(
        uri: String?,
        localName: String?,
        qName: String?,
        attributes: Attributes?,
        writer: DelegatingXMLElementWriter
    ) {
        writer.startElement(uri, localName, qName, attributes)

        val journeyPatternId = attributes?.getValue("id")
        if (journeyPatternId != null) {
            val name = repository.getNameForJourneyPattern(journeyPatternId)
            if (name != null) {
                writer.startElement("", "Name", "Name", null)
                writer.characters(name.toCharArray(), 0, name.length)
                writer.endElement("", "Name", "Name")
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
