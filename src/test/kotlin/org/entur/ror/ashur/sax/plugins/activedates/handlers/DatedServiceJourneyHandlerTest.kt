package org.entur.ror.ashur.sax.plugins.activedates.handlers

import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.ror.ashur.data.TestDataFactory
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesParsingContext
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesRepository
import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

class DatedServiceJourneyHandlerTest {
    val activeDatesRepository = ActiveDatesRepository()
    val handler = DatedServiceJourneyHandler(activeDatesRepository)

    fun createParsingContext(currentServiceJourneyRef: String, currentOperatingDayRef: String) =
        ActiveDatesParsingContext(
            currentServiceJourneyRef = currentServiceJourneyRef,
            currentOperatingDayRef = currentOperatingDayRef
        )

    @Test
    fun testDsjEndElementHandlerConnectJourneysToToOperatingDay() {
        val context = createParsingContext(currentServiceJourneyRef = "sj:1", currentOperatingDayRef = "opd:1")
        val entity = TestDataFactory.defaultEntity(id = "dsj:1", type = NetexTypes.DATED_SERVICE_JOURNEY)
        handler.endElement(context, entity)

        assertTrue(activeDatesRepository.getServiceJourneyData("sj:1").operatingDays.contains("opd:1"))
        assertEquals("opd:1", activeDatesRepository.datedServiceJourneyToOperatingDays.get("dsj:1"))
    }
}