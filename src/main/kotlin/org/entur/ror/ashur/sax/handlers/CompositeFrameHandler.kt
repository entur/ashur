package org.entur.ror.ashur.sax.handlers

import org.entur.netex.tools.lib.extensions.toAttributes
import org.entur.netex.tools.lib.extensions.toMap
import org.entur.netex.tools.lib.output.DelegatingXMLElementWriter
import org.entur.netex.tools.lib.output.XMLElementHandler
import org.xml.sax.Attributes
import java.time.LocalDateTime

/**
 * Handler that adds a "created" attribute to the start element if a creation timestamp is provided.
 * Used to annotate CompositeFrame elements with their creation time.
 *
 * @param fileCreatedAt The creation timestamp to be added as an attribute. If null, the element will be written as is.
 */
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