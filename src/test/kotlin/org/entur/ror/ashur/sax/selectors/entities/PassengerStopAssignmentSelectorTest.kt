package org.entur.ror.ashur.sax.selectors.entities

import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.netex.tools.lib.model.Ref
import org.entur.netex.tools.lib.selectors.entities.EntitySelectorContext
import org.entur.ror.ashur.data.TestDataFactory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PassengerStopAssignmentSelectorTest {
    val passengerStopAssignment1 = TestDataFactory.defaultEntity("psa:1", NetexTypes.PASSENGER_STOP_ASSIGNMENT)
    val passengerStopAssignment2 = TestDataFactory.defaultEntity("psa:2", NetexTypes.PASSENGER_STOP_ASSIGNMENT)

    val stopReferredFromAssignmentOnly = TestDataFactory.defaultEntity("ssp:2", NetexTypes.SCHEDULED_STOP_POINT)
    val stopReferredFromAssignmentAndJourneyPattern = TestDataFactory.defaultEntity("ssp:1", NetexTypes.SCHEDULED_STOP_POINT)

    val stopPointInJourneyPattern1 = TestDataFactory.defaultEntity("spijp:1", NetexTypes.STOP_POINT_IN_JOURNEY_PATTERN)
    val stopPointInJourneyPattern2 = TestDataFactory.defaultEntity("spijp:2", NetexTypes.STOP_POINT_IN_JOURNEY_PATTERN)

    val entityModel = TestDataFactory.defaultEntityModel()

    val entitySelection = TestDataFactory.entitySelection(
        entities = setOf(
            passengerStopAssignment1,
            passengerStopAssignment2,
            stopReferredFromAssignmentAndJourneyPattern,
            stopReferredFromAssignmentOnly,
            stopPointInJourneyPattern1,
            stopPointInJourneyPattern2
        )
    )

    val context = EntitySelectorContext(
        entityModel = entityModel,
        currentEntitySelection = entitySelection,
    )

    @BeforeEach
    fun setUp() {
        entityModel.addEntity(passengerStopAssignment1)
        entityModel.addEntity(passengerStopAssignment2)
        entityModel.addEntity(stopReferredFromAssignmentAndJourneyPattern)
        entityModel.addEntity(stopReferredFromAssignmentOnly)

        entityModel.addRef(Ref("ScheduledStopPointRef", passengerStopAssignment1, stopReferredFromAssignmentAndJourneyPattern.id))
        entityModel.addRef(Ref("ScheduledStopPointRef", stopPointInJourneyPattern1, stopReferredFromAssignmentAndJourneyPattern.id))

        entityModel.addRef(Ref("ScheduledStopPointRef", passengerStopAssignment2, stopReferredFromAssignmentOnly.id))
    }

    @Test
    fun testSelectorExcludesStopPointsWhenOnlyPassengerStopAssignmentsReferToIt() {
        val selection = PassengerStopAssignmentSelector().selectEntities(context)
        assertFalse(selection.includes(passengerStopAssignment2))
        assertFalse(selection.includes(stopReferredFromAssignmentOnly))
    }

    @Test
    fun testSelectorIncludesStopPointWhenOthersThanPassengerStopAssignmentsReferToIt() {
        val selection = PassengerStopAssignmentSelector().selectEntities(context)
        Assertions.assertTrue(selection.includes(passengerStopAssignment1))
        Assertions.assertTrue(selection.includes(stopReferredFromAssignmentAndJourneyPattern))
    }
}