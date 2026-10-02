package org.entur.ror.ashur.sax.selectors.refs

import org.entur.netex.tools.lib.config.TimePeriod
import org.entur.netex.tools.lib.model.Ref
import org.entur.ror.ashur.data.TestDataFactory
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesRepository
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ActiveDatesRefSelectorTest {

    private val period = TimePeriod(LocalDate.of(2026, 10, 1), LocalDate.of(2027, 10, 1))

    @Test
    fun testDayTypeWithoutActiveDatesIsRemoved() {
        val serviceJourney = TestDataFactory.defaultEntity("1", "ServiceJourney")
        val activeDayTypeRef = Ref("DayTypeRef", serviceJourney, "DayType:active")
        val emptyDayTypeRef = Ref("DayTypeRef", serviceJourney, "DayType:empty")

        val entityModel = TestDataFactory.defaultEntityModel()
        entityModel.addEntity(serviceJourney)
        entityModel.addRef(activeDayTypeRef)
        entityModel.addRef(emptyDayTypeRef)

        // DayType:empty has neither DaysOfWeek nor DayTypeAssignments, so it is absent from the repository
        val repository = ActiveDatesRepository()
        repository.getDayTypeData("DayType:active").dates.add(LocalDate.of(2026, 12, 1))
        repository.getServiceJourneyData(serviceJourney.id).dayTypes.addAll(listOf("DayType:active", "DayType:empty"))

        val selection = ActiveDatesRefSelector(repository, period).selectRefs(entityModel)

        assertTrue(selection.includes(activeDayTypeRef))
        assertFalse(selection.includes(emptyDayTypeRef))
    }
}
