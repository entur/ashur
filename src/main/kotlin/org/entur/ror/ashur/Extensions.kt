package org.entur.ror.ashur

import com.google.protobuf.ByteString
import com.google.pubsub.v1.PubsubMessage
import org.apache.camel.Exchange
import org.entur.ror.ashur.Constants.FILE_CREATED_TIMESTAMP_HEADER
import org.entur.ror.ashur.exceptions.InvalidFilterProfileException
import org.entur.ror.ashur.filter.FilterProfile
import java.io.File
import java.time.LocalDateTime

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
    return this.attributesMap[Constants.CORRELATION_ID_HEADER]
}

fun PubsubMessage.getNetexFileName(): String? {
    return this.attributesMap[Constants.NETEX_FILE_NAME_HEADER]
}

fun PubsubMessage.getCodespace(): String? {
    return this.attributesMap[Constants.CODESPACE_HEADER]
}

fun PubsubMessage.getNetexSource(): String? {
    return this.attributesMap[Constants.NETEX_SOURCE_HEADER]
}

fun PubsubMessage.getStatus(): String? {
    return this.attributesMap[Constants.FILTERING_REPORT_STATUS_HEADER]
}

fun PubsubMessage.getReason(): String? {
    return this.attributesMap[Constants.FILTERING_FAILURE_REASON_HEADER]
}

fun PubsubMessage.getFileCreatedTimestamp(): LocalDateTime? {
    val headerAsString = this.attributesMap[FILE_CREATED_TIMESTAMP_HEADER]
    return if (headerAsString != null) {
        LocalDateTime.parse(headerAsString)
    } else {
        null
    }
}

fun PubsubMessage.getPathOfFilteredFile(): String? {
    return this.attributesMap[Constants.FILTERED_NETEX_FILE_PATH_HEADER]
}

fun PubsubMessage.getFilterProfile(): FilterProfile {
    try {
        return FilterProfile.valueOf(this.attributesMap[Constants.FILTERING_PROFILE_HEADER]!!)
    } catch (_: Exception) {
        throw InvalidFilterProfileException(
            "Invalid or missing FilteringProfile attribute in PubsubMessage: ${this.attributesMap[Constants.FILTERING_PROFILE_HEADER]}",
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

    val data = if (bodyAsString != null) {
        ByteString.copyFromUtf8(bodyAsString)
    } else {
        ByteString.EMPTY
    }

    return PubsubMessage
        .newBuilder()
        .setData(data)
        .putAllAttributes(attributes)
        .build()
}