package org.entur.ror.ashur.sax.handlers

import org.entur.netex.tools.lib.output.DelegatingXMLElementWriter
import org.entur.netex.tools.lib.output.XMLElementHandler
import org.entur.ror.ashur.sax.plugins.operatorref.OperatorRefRepository
import org.xml.sax.Attributes
import org.xml.sax.helpers.AttributesImpl

class ServiceJourneyLineRefHandler(
    private val context: ServiceJourneyOperatorRefContext,
    private val repository: OperatorRefRepository
) : XMLElementHandler {

    override fun startElement(
        uri: String?,
        localName: String?,
        qName: String?,
        attributes: Attributes?,
        writer: DelegatingXMLElementWriter
    ) {
        val serviceJourneyId = context.currentServiceJourneyId
        if (serviceJourneyId != null && !repository.hasOperatorRef(serviceJourneyId)) {
            val lineRef = attributes?.getValue("ref")
            if (lineRef != null) {
                val operatorRef = repository.getOperatorRefForLine(lineRef)
                if (operatorRef != null) {
                    val operatorRefAttrs = AttributesImpl()
                    operatorRefAttrs.addAttribute("", "ref", "ref", "CDATA", operatorRef)
                    writer.startElement("", "OperatorRef", "OperatorRef", operatorRefAttrs)
                    writer.endElement("", "OperatorRef", "OperatorRef")
                }
            }
        }
        writer.startElement(uri, localName, qName, attributes)
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
