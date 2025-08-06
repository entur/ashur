package org.entur.ror.ashur

import com.google.pubsub.v1.PubsubMessage

fun PubsubMessage.getCorrelationId(): String? {
    return this.attributesMap["CorrelationId"]
}

fun PubsubMessage.getNetexFileName(): String? {
    return this.attributesMap["NetexFile"]
}

fun PubsubMessage.getCodespace(): String? {
    return this.attributesMap["Codespace"]
}
