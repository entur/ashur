package org.entur.ror.ashur.camel

import org.apache.camel.Exchange
import org.apache.camel.Message
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.google.pubsub.consumer.AcknowledgeCompletion
import org.apache.camel.spi.Synchronization
import org.apache.camel.support.DefaultExchange
import java.util.function.Predicate

abstract class BaseRouteBuilder: RouteBuilder() {
    private val SYNCHRONIZATION_HOLDER: String = "SYNCHRONIZATION_HOLDER"

    /**
     * Remove the PubSub synchronization.
     * This prevents an aggregator from acknowledging the aggregated PubSub messages before the end of the route.
     * In case of failure during the routing this would make it impossible to retry the messages.
     * The synchronization is stored temporarily in a header and is applied again after the aggregation is complete
     *
     * @see .addSynchronizationForAggregatedExchange
     */
    fun removeSynchronizationForAggregatedExchange(e: Exchange) {
        val temporaryExchange = DefaultExchange(e.context)
        e
            .unitOfWork
            .handoverSynchronization(
                temporaryExchange,
                Predicate { obj: Synchronization? -> AcknowledgeCompletion::class.java.isInstance(obj) }
            )
        e.getIn().setHeader(SYNCHRONIZATION_HOLDER, temporaryExchange)
    }

    /**
     * Add back the PubSub synchronization.
     *
     * @see .removeSynchronizationForAggregatedExchange
     */
    protected fun addSynchronizationForAggregatedExchange(aggregatedExchange: Exchange) {
        val messages: MutableList<Message> =
            aggregatedExchange.getIn().getBody(MutableList::class.java) as MutableList<Message>
        for (m in messages) {
            val temporaryExchange = m.getHeader(SYNCHRONIZATION_HOLDER, Exchange::class.java)
            checkNotNull(temporaryExchange) { "Synchronization holder not found" }
            temporaryExchange.exchangeExtension.handoverCompletions(aggregatedExchange)
        }
    }
}