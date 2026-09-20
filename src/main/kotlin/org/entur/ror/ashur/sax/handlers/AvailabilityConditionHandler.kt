package org.entur.ror.ashur.sax.handlers

import org.entur.netex.tools.lib.output.XMLElementHandler

class AvailabilityConditionHandler(private val codespace: String) : XMLElementHandler {
    override fun startElement(
        uri: String?,
        localName: String?,
        qName: String?,
        attributes: org.xml.sax.Attributes?,
        writer: org.entur.netex.tools.lib.output.DelegatingXMLElementWriter
    ) {

        writer.startElement(uri, localName, qName, attributes)
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