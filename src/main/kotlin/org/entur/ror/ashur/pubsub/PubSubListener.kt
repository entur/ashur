package org.entur.ror.ashur.pubsub

interface PubSubListener {
    /**
     * Starts listening to the Pub/Sub subscription.
     * This method blocks until the listener is stopped.
     */
    fun listen()
}