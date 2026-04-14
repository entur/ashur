package org.entur.ror.ashur.sax.plugins.activedates.handlers

import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.ror.ashur.data.TestDataFactory
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesParsingContext
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesRepository
import org.entur.ror.ashur.sax.plugins.activedates.data.VehicleJourneyData
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ArrivalDayOffsetHandlerTest {
    val serviceJourneyEntity = TestDataFactory.defaultEntity(
        type = NetexTypes.SERVICE_JOURNEY,
        id = "TST:ServiceJourney:1",
    )

    val timetabledPassingTimeOnServiceJourney = TestDataFactory.defaultEntity(
        type = NetexTypes.TIMETABLED_PASSING_TIME,
        id = "TST:TimetabledPassingTime:1",
        parent = serviceJourneyEntity,
    )

    val deadRunEntity = TestDataFactory.defaultEntity(
        type = NetexTypes.DEAD_RUN,
        id = "TST:DeadRun:1"
    )

    val timetabledPassingTimeOnDeadRun = TestDataFactory.defaultEntity(
        type = NetexTypes.TIMETABLED_PASSING_TIME,
        id = "TST:TimetabledPassingTime:2",
        parent = deadRunEntity,
    )

    val activeDatesRepository = ActiveDatesRepository(
        deadRuns = mutableMapOf(
            deadRunEntity.id to VehicleJourneyData()
        ),
        serviceJourneys = mutableMapOf(
            serviceJourneyEntity.id to VehicleJourneyData()
        )
    )

    val handler = ArrivalDayOffsetHandler(activeDatesRepository)
    val context = ActiveDatesParsingContext(
        currentDeadRunId = deadRunEntity.id,
        currentServiceJourneyId = serviceJourneyEntity.id,
    )

    @Test
    fun testArrivalDayOffsetIsConnectedToServiceJourney() {
        handler.characters(context, "1".toCharArray(), 0, 1)
        handler.endElement(context, timetabledPassingTimeOnServiceJourney)
        val offsetRegisteredOnServiceJourney = activeDatesRepository.getServiceJourneyData(serviceJourneyEntity.id).finalArrivalDayOffset
        assertEquals(1, offsetRegisteredOnServiceJourney)
    }

    @Test
    fun testArrivalDayOffsetIsConnectedToDeadRun() {
        handler.characters(context, "2".toCharArray(), 0, 1)
        handler.endElement(context, timetabledPassingTimeOnDeadRun)
        val offsetRegisteredOnDeadRun = activeDatesRepository.getDeadRunData(deadRunEntity.id).finalArrivalDayOffset
        assertEquals(2, offsetRegisteredOnDeadRun)
    }
}