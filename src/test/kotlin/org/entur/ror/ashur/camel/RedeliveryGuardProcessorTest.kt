package org.entur.ror.ashur.camel

import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.support.DefaultExchange
import org.entur.ror.ashur.Constants
import org.entur.ror.ashur.exceptions.ClaimHeldException
import org.entur.ror.ashur.exceptions.InvalidFilterProfileException
import org.entur.ror.ashur.filter.FilterProfile
import org.entur.ror.ashur.pubsub.Claim
import org.entur.ror.ashur.pubsub.ClaimHandle
import org.entur.ror.ashur.pubsub.GuardDecision
import org.entur.ror.ashur.pubsub.GuardRequest
import org.entur.ror.ashur.pubsub.GuardResult
import org.entur.ror.ashur.pubsub.RedeliveryGuard
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RedeliveryGuardProcessorTest {

    private val camelContext = DefaultCamelContext()

    private fun exchangeFor(
        fileName: String,
        codespace: String,
        correlationId: String,
        filterProfile: String? = FilterProfile.StandardImportFilter.name,
    ): DefaultExchange {
        val exchange = DefaultExchange(camelContext)
        exchange.getIn().body = ""
        exchange.getIn().setHeader(
            "CamelGooglePubsubAttributes",
            buildMap {
                put(Constants.CODESPACE_HEADER, codespace)
                put(Constants.CORRELATION_ID_HEADER, correlationId)
                put(Constants.NETEX_FILE_NAME_HEADER, fileName)
                filterProfile?.let { put(Constants.FILTERING_PROFILE_HEADER, it) }
            },
        )
        return exchange
    }

    @Test
    fun `PROCESS with no claim handle leaves the exchange untouched`() {
        val guard = mock<RedeliveryGuard> { on { evaluate(any(), any()) } doReturn GuardResult(GuardDecision.PROCESS) }
        val exchange = exchangeFor("data.zip", "RUT", "corr-1")

        RedeliveryGuardProcessor(guard).process(exchange)

        assertNull(exchange.getIn().getHeader(Constants.GUARD_DECISION_HEADER))
        assertNull(exchange.getIn().getHeader(Constants.CLAIM_HANDLE_HEADER))
    }

    @Test
    fun `PROCESS with a claim handle stashes it on the exchange`() {
        val handle = ClaimHandle("claims/RUT/corr-1/StandardImportFilter", "RUT", generation = 7L, claim = Claim("pod-a", 1_000, 1))
        val guard = mock<RedeliveryGuard> { on { evaluate(any(), any()) } doReturn GuardResult(GuardDecision.PROCESS, handle) }
        val exchange = exchangeFor("data.zip", "RUT", "corr-1")

        RedeliveryGuardProcessor(guard).process(exchange)

        assertEquals(handle, exchange.getIn().getHeader(Constants.CLAIM_HANDLE_HEADER))
    }

    @Test
    fun `SKIP sets the skip decision header`() {
        val guard = mock<RedeliveryGuard> { on { evaluate(any(), any()) } doReturn GuardResult(GuardDecision.SKIP) }
        val exchange = exchangeFor("data.zip", "RUT", "corr-1")

        RedeliveryGuardProcessor(guard).process(exchange)

        assertEquals(Constants.GUARD_DECISION_SKIP, exchange.getIn().getHeader(Constants.GUARD_DECISION_HEADER))
    }

    @Test
    fun `BOUNCE throws ClaimHeldException`() {
        val guard = mock<RedeliveryGuard> { on { evaluate(any(), any()) } doReturn GuardResult(GuardDecision.BOUNCE) }
        val exchange = exchangeFor("data.zip", "RUT", "corr-1")

        assertThrows<ClaimHeldException> { RedeliveryGuardProcessor(guard).process(exchange) }
    }

    @Test
    fun `derives the guard request from message attributes`() {
        val guard = mock<RedeliveryGuard> { on { evaluate(any(), any()) } doReturn GuardResult(GuardDecision.PROCESS) }
        val exchange = exchangeFor(
            "inbound/data.zip",
            "RUT",
            "corr-1",
            filterProfile = FilterProfile.IncludeBlocksAndRestrictedJourneysFilter.name,
        )

        RedeliveryGuardProcessor(guard).process(exchange)

        val captor = argumentCaptor<GuardRequest>()
        org.mockito.kotlin.verify(guard).evaluate(captor.capture(), any())
        assertEquals("RUT", captor.firstValue.codespace)
        assertEquals("corr-1", captor.firstValue.correlationId)
        assertEquals(FilterProfile.IncludeBlocksAndRestrictedJourneysFilter, captor.firstValue.filterProfile)
    }

    @Test
    fun `an unparseable filter profile surfaces the pipeline's own validation error`() {
        // Not swallowed here: InvalidFilterProfileException is what the route's generic exception
        // handler turns into a FAILED status, exactly as it would if the handler had thrown it.
        val guard = mock<RedeliveryGuard>()
        val exchange = exchangeFor("data.zip", "RUT", "corr-1", filterProfile = "NotAProfile")

        assertThrows<InvalidFilterProfileException> { RedeliveryGuardProcessor(guard).process(exchange) }

        org.mockito.kotlin.verifyNoInteractions(guard)
    }

    @Test
    fun `a missing correlationId is passed through as absent, not as a placeholder`() {
        val guard = mock<RedeliveryGuard> { on { evaluate(any(), any()) } doReturn GuardResult(GuardDecision.PROCESS) }
        val exchange = DefaultExchange(camelContext)
        exchange.getIn().body = ""
        exchange.getIn().setHeader(
            "CamelGooglePubsubAttributes",
            mapOf(
                Constants.CODESPACE_HEADER to "RUT",
                Constants.NETEX_FILE_NAME_HEADER to "data.zip",
                Constants.FILTERING_PROFILE_HEADER to FilterProfile.StandardImportFilter.name,
            ),
        )

        RedeliveryGuardProcessor(guard).process(exchange)

        val captor = argumentCaptor<GuardRequest>()
        org.mockito.kotlin.verify(guard).evaluate(captor.capture(), any())
        // Substituting UNKNOWN_CORRELATION_ID here would make one claim key shared by every unrelated
        // correlationId-less request for this codespace+profile.
        assertNull(captor.firstValue.correlationId)
    }
}
