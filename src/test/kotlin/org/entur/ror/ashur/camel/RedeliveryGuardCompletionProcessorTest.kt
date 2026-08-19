package org.entur.ror.ashur.camel

import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.support.DefaultExchange
import org.entur.ror.ashur.Constants
import org.entur.ror.ashur.pubsub.Claim
import org.entur.ror.ashur.pubsub.ClaimHandle
import org.entur.ror.ashur.pubsub.RedeliveryGuard
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import kotlin.test.Test

class RedeliveryGuardCompletionProcessorTest {

    private val camelContext = DefaultCamelContext()

    private fun exchange(): DefaultExchange {
        val exchange = DefaultExchange(camelContext)
        exchange.getIn().body = ""
        return exchange
    }

    @Test
    fun `marks the claim completed when a claim handle is present`() {
        val handle = ClaimHandle("claims/RUT/corr-1/StandardImportFilter", "RUT", generation = 7L, claim = Claim("pod-a", 1_000, 1))
        val guard = mock<RedeliveryGuard>()
        val ex = exchange()
        ex.getIn().setHeader(Constants.CLAIM_HANDLE_HEADER, handle)

        RedeliveryGuardCompletionProcessor(guard).process(ex)

        verify(guard).markCompleted(handle)
    }

    @Test
    fun `does nothing when no claim handle is present`() {
        val guard = mock<RedeliveryGuard>()
        val ex = exchange()

        RedeliveryGuardCompletionProcessor(guard).process(ex)

        verifyNoInteractions(guard)
    }
}
