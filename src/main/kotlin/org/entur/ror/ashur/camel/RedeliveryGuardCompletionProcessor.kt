package org.entur.ror.ashur.camel

import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.entur.ror.ashur.Constants
import org.entur.ror.ashur.pubsub.ClaimHandle
import org.entur.ror.ashur.pubsub.RedeliveryGuard
import org.springframework.stereotype.Component

/**
 * Marks the redelivery guard's claim as completed once every externally-visible effect of a
 * successful run (bucket upload, exchange-bucket copy, SUCCEEDED status publish) has actually
 * happened — must run as the very last step of the success path, after the SUCCEEDED publish.
 * No-op if the guard never claimed anything for this delivery (guard disabled, or the message was
 * skipped/bounced before a claim was ever taken).
 */
@Component
class RedeliveryGuardCompletionProcessor(
    private val redeliveryGuard: RedeliveryGuard,
) : Processor {
    override fun process(exchange: Exchange) {
        val handle = exchange.getIn().getHeader(Constants.CLAIM_HANDLE_HEADER, ClaimHandle::class.java) ?: return
        redeliveryGuard.markCompleted(handle)
    }
}
