package org.entur.ror.ashur.pubsub

interface MessageHandler {
    /**
     * Handles the incoming message from a Pub/Sub topic.
     *
     * @param message The Pub/Sub message to handle.
     */
    fun handleMessage(message: com.google.pubsub.v1.PubsubMessage): String
}