package org.entur.ror.ashur.camel

import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.entur.ror.ashur.Constants
import org.entur.ror.ashur.exceptions.ClaimHeldException
import org.entur.ror.ashur.filteredOutputObjectPath
import org.entur.ror.ashur.getCodespace
import org.entur.ror.ashur.getCorrelationId
import org.entur.ror.ashur.getNetexFileName
import org.entur.ror.ashur.pubsub.GuardDecision
import org.entur.ror.ashur.pubsub.GuardRequest
import org.entur.ror.ashur.pubsub.RedeliveryGuard
import org.entur.ror.ashur.toPubsubMessage
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Runs the [RedeliveryGuard] at the very start of the route (before STARTED is published) and
 * translates its decision into route behavior:
 * - PROCESS  -> do nothing; the route continues into the normal pipeline.
 * - SKIP     -> set [Constants.GUARD_DECISION_HEADER]; the route stops (ack) without re-processing.
 * - BOUNCE   -> throw [ClaimHeldException]; the route nacks so Pub/Sub redelivers later.
 *
 * If codespace / correlationId / file handle are missing we degrade to processing (as today) and let
 * the downstream pipeline handle the malformed message — the guard never introduces a new failure.
 */
@Component
class RedeliveryGuardProcessor(
    private val redeliveryGuard: RedeliveryGuard,
) : Processor {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun process(exchange: Exchange) {
        val message = exchange.toPubsubMessage()
        val codespace = message.getCodespace()
        val correlationId = message.getCorrelationId()
        val fileName = message.getNetexFileName()

        if (codespace == null || correlationId == null || fileName == null) {
            logger.warn(
                "Redelivery guard: missing attribute(s) (codespace={}, correlationId={}, fileName={}); skipping guard and processing.",
                codespace, correlationId, fileName,
            )
            return
        }

        val request = GuardRequest(
            codespace = codespace,
            correlationId = correlationId,
            outputPath = filteredOutputObjectPath(codespace, correlationId, fileName),
        )

        when (redeliveryGuard.evaluate(request)) {
            GuardDecision.PROCESS -> Unit
            GuardDecision.SKIP -> exchange.getIn().setHeader(Constants.GUARD_DECISION_HEADER, Constants.GUARD_DECISION_SKIP)
            GuardDecision.BOUNCE -> throw ClaimHeldException(
                "Request codespace=$codespace correlationId=$correlationId is already being processed; bouncing for redelivery.",
            )
        }
    }
}
