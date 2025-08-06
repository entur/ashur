package org.entur.ror.ashur.pubsub

interface MessageHandler {
    /**
     * Handles the incoming message from the Pub/Sub topic.
     *
     * @param message The Pub/Sub message to handle.
     * @param consumer The consumer to acknowledge or nack the message.
     */
    fun handleMessage(message: com.google.pubsub.v1.PubsubMessage, consumer: com.google.cloud.pubsub.v1.AckReplyConsumer)
}