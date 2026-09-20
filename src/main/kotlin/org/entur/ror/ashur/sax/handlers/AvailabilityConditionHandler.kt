package org.entur.ror.ashur.sax.handlers

import org.entur.netex.tools.lib.extensions.addNewAttribute
import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.netex.tools.lib.output.DelegatingXMLElementWriter
import org.entur.netex.tools.lib.output.NetexIdGenerator
import org.entur.netex.tools.lib.output.XMLElementHandler
import org.xml.sax.helpers.AttributesImpl
import java.time.LocalDate

class AvailabilityConditionHandler(
    private val codespace: String,
    private val fromDate: LocalDate,
    private val toDate: LocalDate,
) : XMLElementHandler {
    override fun startElement(
        uri: String?,
        localName: String?,
        qName: String?,
        attributes: org.xml.sax.Attributes?,
        writer: DelegatingXMLElementWriter
    ) {
        val id = NetexIdGenerator.next(codespace.uppercase(), NetexTypes.AVAILABILITY_CONDITION)
        val newAttributes = AttributesImpl()
        newAttributes.addNewAttribute("id", id)
        writer.startElement(uri, localName, qName, newAttributes)

        writeDateField(NetexTypes.FROM_DATE, fromDate, writer)
        writeDateField(NetexTypes.TO_DATE, toDate, writer)
    }

    fun writeDateField(fieldName: String, date: LocalDate, writer: DelegatingXMLElementWriter) {
        writer.startElement("", fieldName, fieldName, null)
        val dateString = date.toString()
        writer.characters(dateString.toCharArray(), 0, dateString.length)
        writer.endElement("", fieldName, fieldName)
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