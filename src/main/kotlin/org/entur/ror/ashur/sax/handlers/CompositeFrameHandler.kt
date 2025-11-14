package org.entur.ror.ashur.sax.handlers

import org.entur.netex.tools.lib.extensions.toAttributes
import org.entur.netex.tools.lib.extensions.toMap
import org.entur.netex.tools.lib.output.DelegatingXMLElementWriter
import org.entur.netex.tools.lib.output.XMLElementHandler
import org.xml.sax.Attributes
import java.time.LocalDateTime

class CompositeFrameHandler(
    private val fileCreatedAt: LocalDateTime?
): XMLElementHandler {
    override fun startElement(
        uri: String?,
        localName: String?,
        qName: String?,
        attributes: Attributes?,
        writer: DelegatingXMLElementWriter
    ) {
        if (fileCreatedAt != null) {
            val existingAttrsMap = (attributes?.toMap() ?: emptyMap()).toMutableMap()
            existingAttrsMap["created"] = fileCreatedAt.toString()
            writer.startElement(uri, localName, qName, existingAttrsMap.toAttributes())
        } else {
            writer.startElement(uri, localName, qName, attributes)
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