package org.entur.ror.ashur

import com.google.protobuf.ByteString
import com.google.pubsub.v1.PubsubMessage
import org.apache.camel.Exchange

fun PubsubMessage.getCorrelationId(): String? {
    return this.attributesMap["CorrelationId"]
}

fun PubsubMessage.getNetexFileName(): String? {
    return this.attributesMap["NetexFile"]
}

fun PubsubMessage.getCodespace(): String? {
    return this.attributesMap["Codespace"]
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
        .putAllAttributes(attributes as Map<String, String>)
        .build()
}