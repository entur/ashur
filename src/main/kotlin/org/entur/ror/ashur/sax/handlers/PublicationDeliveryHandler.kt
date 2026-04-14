package org.entur.ror.ashur.sax.handlers

import org.entur.netex.tools.lib.extensions.toAttributes
import org.entur.netex.tools.lib.output.DelegatingXMLElementWriter
import org.entur.netex.tools.lib.output.XMLElementHandler
import org.xml.sax.Attributes

/**
 * Handler that normalizes the PublicationDelivery root element by replacing
 * all existing attributes and namespace declarations with a standardized set.
 *
 * Any attributes or namespaces present in the input are removed entirely and
 * replaced with exactly:
 * <PublicationDelivery
 *   xmlns="http://www.netex.org.uk/netex"
 *   xmlns:gis="http://www.opengis.net/gml/3.2"
 *   xmlns:siri="http://www.siri.org.uk/siri"
 *   version="1.15:NO-NeTEx-networktimetable:1.5">
 */
class PublicationDeliveryHandler : XMLElementHandler {

    companion object {
        private const val NETEX_NAMESPACE = "http://www.netex.org.uk/netex"
        private const val GIS_PREFIX = "gis"
        private const val GIS_NAMESPACE = "http://www.opengis.net/gml/3.2"
        private const val SIRI_PREFIX = "siri"
        private const val SIRI_NAMESPACE = "http://www.siri.org.uk/siri"
        private const val NETEX_VERSION = "1.15:NO-NeTEx-networktimetable:1.5"
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

        // Replace all existing attributes with only the version attribute
        val newAttributes = mapOf("version" to NETEX_VERSION)

        writer.startElement(uri, localName, qName, newAttributes.toAttributes())
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
