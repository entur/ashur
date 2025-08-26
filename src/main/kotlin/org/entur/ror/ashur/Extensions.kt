package org.entur.ror.ashur

import com.google.protobuf.ByteString
import com.google.pubsub.v1.PubsubMessage
import org.apache.camel.Exchange
import org.entur.ror.ashur.exceptions.InvalidFilterProfileException
import org.entur.ror.ashur.filter.FilterProfile
import java.io.File

fun File.createFileWithDirectories(): File {
    if (!this.exists()) {
        if (!this.parentFile.exists()) {
            this.parentFile?.mkdirs()
        }
        this.createNewFile()
    }
    return this
}

fun PubsubMessage.getCorrelationId(): String? {
    return this.attributesMap["RutebankenCorrelationId"]
}

fun PubsubMessage.getNetexFileName(): String? {
    return this.attributesMap["RutebankenFileHandle"]
}

fun PubsubMessage.getCodespace(): String? {
    return this.attributesMap["EnturDatasetReferential"]
}

fun PubsubMessage.getNetexSource(): String? {
    return this.attributesMap["NetexSource"]
}

fun PubsubMessage.getFilterProfile(): FilterProfile {
    try {
        return FilterProfile.valueOf(this.attributesMap["FilterProfile"]!!)
    } catch (_: Exception) {
        throw InvalidFilterProfileException(
            "Invalid or missing FilteringProfile attribute in PubsubMessage: ${this.attributesMap["FilterProfile"]}",
        )
    }
}

fun Exchange.getPubsubAttributes(): Map<String, String> {
    val attributes = this.getIn().headers["CamelGooglePubsubAttributes"]
    if (attributes !is Map<*, *>) {
        throw IllegalArgumentException("Expected CamelGooglePubsubAttributes to be a Map, but got: $attributes")
    }
    if (attributes.isEmpty()) {
        throw IllegalArgumentException("CamelGooglePubsubAttributes cannot be empty")
    }
    return attributes.map { it.key.toString() to it.value.toString() }.toMap()
}

/**
 * Converts a Camel Exchange to a PubsubMessage.
 *
 * @return A PubsubMessage containing the body and attributes from the Exchange.
 */
fun Exchange.toPubsubMessage(): PubsubMessage {
    val bodyAsString = this.getIn().getBody(String::class.java)
    val attributes = this.getPubsubAttributes()

    return PubsubMessage
        .newBuilder()
        .setData(ByteString.copyFromUtf8(bodyAsString))
        .putAllAttributes(attributes)
        .build()
}