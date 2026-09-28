package org.entur.ror.ashur.sax.handlers

import org.entur.netex.tools.lib.output.DelegatingXMLElementWriter
import org.entur.netex.tools.lib.output.XMLElementHandler
import org.xml.sax.Attributes

/**
 * The filtering done by Ashur implicitly scopes any dataset being imported to
 * a pre-defined validity period, by filtering away journeys that do not run
 * inside that same window.
 *
 * A codespace may import NeTEx data that has set an explicit AvailabilityCondition that
 * differs from this period. This handler specifically, ensures that the FromDate field
 * from AvailabilityCondition is entirely controlled by Ashur, and not written based on
 * the FromDate received.
 *
 * See @AvailabilityConditionHandler for how FromDate is written explicitly by Ashur.
 **/
class AvailabilityConditionFromDateHandler: XMLElementHandler {
    override fun startElement(
        uri: String?,
        localName: String?,
        qName: String?,
        attributes: Attributes?,
        writer: DelegatingXMLElementWriter
    ) {
        // noop
    }

    override fun characters(
        ch: CharArray?,
        start: Int,
        length: Int,
        writer: DelegatingXMLElementWriter
    ) {
        // noop
    }

    override fun endElement(
        uri: String?,
        localName: String?,
        qName: String?,
        writer: DelegatingXMLElementWriter
    ) {
        // noop
    }
}