package org.entur.ror.ashur.sax.plugins.activedates.handlers

import org.entur.ror.ashur.data.TestDataFactory
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesParsingContext
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesRepository
import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

class ServiceJourneyHandlerTest {
    val context = ActiveDatesParsingContext()
    val repo = ActiveDatesRepository()
    val handler = ServiceJourneyHandler(repo)

    @Test
    fun testServiceJourneyHandlerCollectsServiceJourneyId() {
        val serviceJourneyEntity = TestDataFactory.defaultEntity(
            id = "TST:ServiceJourney:1",
        )

        handler.startElement(
            context = context,
            attributes = null,
            currentEntity = serviceJourneyEntity,
        )

        assertEquals(
            serviceJourneyEntity.id,
            context.currentServiceJourneyId,
        )
    }

    @Test
    fun testServiceJourneyHandlerClearsServiceJourneyIdOnEndElement() {
        val serviceJourneyEntity = TestDataFactory.defaultEntity(
            id = "TST:ServiceJourney:1",
        )

        handler.startElement(
            context = context,
            attributes = null,
            currentEntity = serviceJourneyEntity,
        )

        handler.endElement(
            context = context,
            currentEntity = serviceJourneyEntity,
        )

        assertNull(context.currentServiceJourneyId)
    }
}