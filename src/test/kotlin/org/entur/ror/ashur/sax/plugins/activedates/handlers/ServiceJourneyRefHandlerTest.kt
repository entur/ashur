package org.entur.ror.ashur.sax.plugins.activedates.handlers

import org.entur.netex.tools.lib.extensions.toAttributes
import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.ror.ashur.data.TestDataFactory
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesParsingContext
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesRepository
import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

class ServiceJourneyRefHandlerTest {
    val context = ActiveDatesParsingContext()
    val repo = ActiveDatesRepository()
    val handler = ServiceJourneyRefHandler(repo)

    @Test
    fun testServiceJourneyRefHandlerCollectsServiceJourneyRefForDatedServiceJourney() {
        val currentEntity = TestDataFactory.defaultEntity(
            id = "TST:DatedServiceJourney:1",
            type = NetexTypes.DATED_SERVICE_JOURNEY
        )

        handler.startElement(
            context = context,
            attributes = mapOf("ref" to "TST:ServiceJourney:1").toAttributes(),
            currentEntity = currentEntity,
        )

        assertEquals(
            "TST:ServiceJourney:1",
            context.currentServiceJourneyRef,
        )
    }
}