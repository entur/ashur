package org.entur.ror.ashur.sax.handlers

import org.entur.netex.tools.lib.output.DelegatingXMLElementWriter
import org.entur.netex.tools.lib.output.XMLElementHandler
import org.xml.sax.Attributes

class CodespacesHandler: XMLElementHandler {
    override fun startElement(
        uri: String?,
        localName: String?,
        qName: String?,
        attributes: Attributes?,
        writer: DelegatingXMLElementWriter
    ) {
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

        writer.startElement("", "", "FrameDefaults", null)
        writer.startElement("", "", "DefaultLocale", null)

        val timezone = "CET"
        writer.startElement("", "", "TimeZone", null)
        writer.characters(timezone.toCharArray(), 0, timezone.length)
        writer.endElement("", "", "TimeZone")

        val defaultLanguage = "no"
        writer.startElement("", "", "DefaultLanguage", null)
        writer.characters(defaultLanguage.toCharArray(), 0, defaultLanguage.length)
        writer.endElement("", "", "DefaultLanguage")

        writer.endElement("", "", "DefaultLocale")
        writer.endElement("", "", "FrameDefaults")
    }
}