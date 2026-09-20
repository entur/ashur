package org.entur.ror.ashur.sax.handlers

import org.entur.netex.tools.lib.extensions.addNewAttribute
import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.netex.tools.lib.output.NetexIdGenerator
import org.entur.netex.tools.lib.output.XMLElementHandler
import org.xml.sax.helpers.AttributesImpl

class AvailabilityConditionHandler(
    private val codespace: String
) : XMLElementHandler {
    override fun startElement(
        uri: String?,
        localName: String?,
        qName: String?,
        attributes: org.xml.sax.Attributes?,
        writer: org.entur.netex.tools.lib.output.DelegatingXMLElementWriter
    ) {
        val id = NetexIdGenerator.next(codespace.uppercase(), NetexTypes.AVAILABILITY_CONDITION)
        val newAttributes = AttributesImpl()
        newAttributes.addNewAttribute("id", id)
        writer.startElement(uri, localName, qName, newAttributes)

        writer.startElement("", "FromDate", "FromDate", null)
        writer.characters("2026-01-01".toCharArray(), 0, "2026-01-01".length)
        writer.endElement("", "FromDate", "FromDate")
        writer.startElement("", "ToDate", "ToDate", null)
        writer.characters("2026-01-02".toCharArray(), 0, "2026-01-02".length)
        writer.endElement("", "ToDate", "ToDate")
    }

    override fun characters(
        ch: CharArray?,
        start: Int,
        length: Int,
        writer: org.entur.netex.tools.lib.output.DelegatingXMLElementWriter
    ) {
        writer.characters(ch, start, length)
    }

    override fun endElement(
        uri: String?,
        localName: String?,
        qName: String?,
        writer: org.entur.netex.tools.lib.output.DelegatingXMLElementWriter
    ) {
        writer.endElement(uri, localName, qName)
    }
}