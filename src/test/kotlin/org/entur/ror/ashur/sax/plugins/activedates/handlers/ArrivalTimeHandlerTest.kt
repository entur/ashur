package org.entur.ror.ashur.sax.plugins.activedates.handlers

import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.ror.ashur.data.TestDataFactory
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesParsingContext
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesRepository
import org.entur.ror.ashur.sax.plugins.activedates.data.VehicleJourneyData
import org.junit.Test
import java.time.LocalTime
import kotlin.test.assertEquals

class ArrivalTimeHandlerTest {
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

    val handler = ArrivalTimeHandler(activeDatesRepository)
    val context = ActiveDatesParsingContext(
        currentDeadRunId = deadRunEntity.id,
        currentServiceJourneyId = serviceJourneyEntity.id,
    )

    @Test
    fun testArrivalTimeIsConnectedToServiceJourney() {
        val time = LocalTime.of(10, 0, 0).toString()
        handler.characters(context, time.toCharArray(), 0, time.length)
        handler.endElement(context, timetabledPassingTimeOnServiceJourney)
        val arrivalTimeRegisteredOnServiceJourney = activeDatesRepository.getServiceJourneyData(serviceJourneyEntity.id).finalArrivalTime
        assertEquals(LocalTime.parse("10:00"), arrivalTimeRegisteredOnServiceJourney)
    }

    @Test
    fun testArrivalDayOffsetIsConnectedToDeadRun() {
        val time = LocalTime.of(10, 15, 0).toString()
        handler.characters(context, time.toCharArray(), 0, time.length)
        handler.endElement(context, timetabledPassingTimeOnDeadRun)
        val arrivalTimeRegisteredOnDeadRun = activeDatesRepository.getDeadRunData(deadRunEntity.id).finalArrivalTime
        assertEquals(LocalTime.parse("10:15"), arrivalTimeRegisteredOnDeadRun)
    }
}