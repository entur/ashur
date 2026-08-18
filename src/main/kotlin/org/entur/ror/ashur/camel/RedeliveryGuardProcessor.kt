package org.entur.ror.ashur.camel

import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.entur.ror.ashur.Constants
import org.entur.ror.ashur.exceptions.ClaimHeldException
import org.entur.ror.ashur.getCodespace
import org.entur.ror.ashur.getCorrelationId
import org.entur.ror.ashur.getFilterProfile
import org.entur.ror.ashur.pubsub.GuardDecision
import org.entur.ror.ashur.pubsub.GuardRequest
import org.entur.ror.ashur.pubsub.RedeliveryGuard
import org.entur.ror.ashur.toPubsubMessage
import org.springframework.stereotype.Component

/**
 * Runs the [RedeliveryGuard] at the very start of the route (before STARTED is published) and
 * translates its decision into route behavior:
 * - PROCESS  -> stash the claim handle (if any) on [Constants.CLAIM_HANDLE_HEADER] so
 *   [RedeliveryGuardCompletionProcessor] can mark it completed later; the route continues normally.
 * - SKIP     -> set [Constants.GUARD_DECISION_HEADER]; the route stops (ack) without re-processing.
 * - BOUNCE   -> throw [ClaimHeldException]; the route nacks so Pub/Sub redelivers later.
 *
 * Malformed messages are deliberately not special-cased here: codespace and filterProfile are already
 * validated downstream (the handler dereferences codespace, and [getFilterProfile] throws
 * [org.entur.ror.ashur.exceptions.InvalidFilterProfileException]), and both land on the route's
 * generic exception handler as a FAILED status. Re-checking them here would only swallow those
 * validations and re-raise the same thing a few steps later.
 *
 * correlationId is the exception: the handler tolerates a missing one by falling back to "unknown", so
 * the guard mirrors that fallback rather than failing or bailing out — otherwise such a message would
 * run entirely unguarded.
 */
@Component
class RedeliveryGuardProcessor(
    private val redeliveryGuard: RedeliveryGuard,
) : Processor {
    override fun process(exchange: Exchange) {
        val message = exchange.toPubsubMessage()
        val codespace = message.getCodespace()!!
        val correlationId = message.getCorrelationId() ?: Constants.UNKNOWN_CORRELATION_ID
        val filterProfile = message.getFilterProfile()

        val request = GuardRequest(
            codespace = codespace,
            correlationId = correlationId,
            filterProfile = filterProfile,
        )
        val result = redeliveryGuard.evaluate(request)

        when (result.decision) {
            GuardDecision.PROCESS -> result.claimHandle?.let { exchange.getIn().setHeader(Constants.CLAIM_HANDLE_HEADER, it) }
            GuardDecision.SKIP -> exchange.getIn().setHeader(Constants.GUARD_DECISION_HEADER, Constants.GUARD_DECISION_SKIP)
            GuardDecision.BOUNCE -> throw ClaimHeldException(
                "Request codespace=$codespace correlationId=$correlationId filterProfile=${filterProfile.name} is already being processed; bouncing for redelivery.",
            )
        }
    }
}
