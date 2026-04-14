package org.entur.ror.ashur.pubsub

import org.entur.ror.ashur.filter.FilterResult

interface MessageHandler {
    /**
     * Handles the incoming message from a Pub/Sub topic.
     *
     * @param message The Pub/Sub message to handle.
     */
    fun handleMessage(message: com.google.pubsub.v1.PubsubMessage): FilterResult
}