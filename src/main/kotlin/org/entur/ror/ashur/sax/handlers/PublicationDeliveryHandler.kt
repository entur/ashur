package org.entur.ror.ashur.sax.handlers

import org.entur.netex.tools.lib.output.DelegatingXMLElementWriter
import org.entur.netex.tools.lib.output.XMLElementHandler
import org.xml.sax.Attributes
import org.xml.sax.helpers.AttributesImpl

/**
 * Handler that normalizes the PublicationDelivery root element by preserving
 * input attributes while overriding namespace declarations and the version attribute.
 *
 * Specifically:
 * - Namespace declarations are replaced with a standardized set (netex, gis, siri)
 * - The version attribute is overridden with a standardized value
 * - The xsi:schemaLocation attribute is removed
 * - All other input attributes are preserved as-is
 */
class PublicationDeliveryHandler : XMLElementHandler {

    companion object {
        private const val NETEX_NAMESPACE = "http://www.netex.org.uk/netex"
        private const val GIS_PREFIX = "gis"
        private const val GIS_NAMESPACE = "http://www.opengis.net/gml/3.2"
        private const val SIRI_PREFIX = "siri"
        private const val SIRI_NAMESPACE = "http://www.siri.org.uk/siri"
        private const val NETEX_VERSION = "1.15:NO-NeTEx-networktimetable:1.5"
        private const val XSI_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance"
    }

    override fun startElement(
        uri: String?,
        localName: String?,
        qName: String?,
        attributes: Attributes?,
        writer: DelegatingXMLElementWriter
    ) {
        // Declare namespace prefixes before the element (empty prefix for default namespace)
        writer.startPrefixMapping("", NETEX_NAMESPACE)
        writer.startPrefixMapping(GIS_PREFIX, GIS_NAMESPACE)
        writer.startPrefixMapping(SIRI_PREFIX, SIRI_NAMESPACE)

        writer.startElement(uri, localName, qName, normalizeAttributes(attributes))
    }

    private fun normalizeAttributes(attributes: Attributes?): AttributesImpl {
        val normalized = AttributesImpl(attributes)

        val schemaLocationIndex = normalized.getIndex(XSI_NAMESPACE, "schemaLocation")
        if (schemaLocationIndex >= 0) {
            normalized.removeAttribute(schemaLocationIndex)
        }

        // Also remove by qName in case namespace URI is not populated by the parser
        val schemaLocationByQName = normalized.getIndex("xsi:schemaLocation")
        if (schemaLocationByQName >= 0) {
            normalized.removeAttribute(schemaLocationByQName)
        }

        val versionIndex = normalized.getIndex("version")
        if (versionIndex >= 0) {
            normalized.setValue(versionIndex, NETEX_VERSION)
        } else {
            normalized.addAttribute("", "version", "version", "CDATA", NETEX_VERSION)
        }

        return normalized
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

        // End namespace scopes after the element (reverse order of declaration)
        writer.endPrefixMapping(SIRI_PREFIX)
        writer.endPrefixMapping(GIS_PREFIX)
        writer.endPrefixMapping("")
    }
}
