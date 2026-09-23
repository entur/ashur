package org.entur.ror.ashur.sax.handlers

import org.entur.netex.tools.lib.extensions.toISO8601
import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.netex.tools.lib.output.DelegatingXMLElementWriter
import org.entur.netex.tools.lib.output.XMLElementHandler
import org.xml.sax.Attributes
import java.time.LocalDate

/**
 * Writes the AvailabilityCondition entity, using a pre-defined FromDate.
 *
 * The FromDate value is the same one used to determine the start of the window when
 * filtering journeys in the past.
 **/
class AvailabilityConditionHandler(private val fromDate: LocalDate) : XMLElementHandler {
    private val fromDateField = NetexTypes.FROM_DATE

    override fun startElement(
        uri: String?,
        localName: String?,
        qName: String?,
        attributes: Attributes?,
        writer: DelegatingXMLElementWriter
    ) {
        writer.startElement(uri, localName, qName, attributes)
        writeFromDateField(fromDate, writer)
    }

    private fun writeFromDateField(date: LocalDate, writer: DelegatingXMLElementWriter) {
        writer.startElement("", fromDateField, fromDateField, null)
        val dateString = date.toISO8601()
        writer.characters(dateString.toCharArray(), 0, dateString.length)
        writer.endElement("", fromDateField, fromDateField)
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