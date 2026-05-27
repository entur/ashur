package org.entur.ror.ashur.camel

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import net.logstash.logback.marker.LogstashMarker
import org.slf4j.Marker
import java.io.StringWriter

private val mapper = jacksonObjectMapper()

internal fun Marker.fieldMap(): Map<String, Any?> {
    val out = StringWriter()
    mapper.factory.createGenerator(out).use { gen ->
        gen.writeStartObject()
        (this as LogstashMarker).writeTo(gen)
        gen.writeEndObject()
    }
    @Suppress("UNCHECKED_CAST")
    return mapper.readValue(out.toString(), Map::class.java) as Map<String, Any?>
}
