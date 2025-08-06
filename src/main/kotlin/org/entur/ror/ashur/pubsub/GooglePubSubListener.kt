package org.entur.ror.ashur.pubsub

import com.google.cloud.pubsub.v1.MessageReceiver
import com.google.cloud.pubsub.v1.Subscriber
import com.google.pubsub.v1.ProjectSubscriptionName
import org.slf4j.LoggerFactory
import java.util.concurrent.CountDownLatch

class GooglePubSubListener(
    projectId: String,
    private val subscriptionId: String,
    private val messageHandler: (message: com.google.pubsub.v1.PubsubMessage, consumer: com.google.cloud.pubsub.v1.AckReplyConsumer) -> Unit
): PubSubListener {
    private val log = LoggerFactory.getLogger(javaClass)

    private val subscriber = Subscriber.newBuilder(
        ProjectSubscriptionName.of(projectId, subscriptionId),
        MessageReceiver { message, consumer -> messageHandler }
    ).build()

    override fun listen() {
        val latch = CountDownLatch(1)
        subscriber.startAsync().awaitRunning()
        log.info("Listening to subscription `$subscriptionId` on GCP...")

        Runtime.getRuntime().addShutdownHook(Thread {
            log.info("Shutting down listener...")
            subscriber.stopAsync().awaitTerminated()
            latch.countDown()
        })

        latch.await()
    }
}