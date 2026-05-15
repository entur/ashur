package org.entur.ror.ashur.sax.plugins.activedates

import org.entur.ror.ashur.data.TestDataFactory
import org.entur.ror.ashur.filter.BaseFilteringProfileConfig
import org.entur.ror.ashur.sax.plugins.activedates.data.DayTypeData
import org.entur.ror.ashur.sax.plugins.activedates.data.OperatingPeriodData
import org.entur.ror.ashur.sax.plugins.activedates.data.VehicleJourneyData
import org.entur.ror.ashur.sax.plugins.activedates.helper.ActiveEntitiesCollector
import org.entur.ror.ashur.sax.plugins.activedates.model.Period
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.LocalDate

class ActiveDatesCalculatorTest {

    private val today = LocalDate.now()

    // Use the same time period as the actual StandardImportFilteringProfileConfig
    private val standardTimePeriod = BaseFilteringProfileConfig.standardTimePeriod()

    // ==================== DATED SERVICE JOURNEY TESTS ====================

    @Nested
    inner class DatedServiceJourneyTests {

        @Test
        fun `DatedServiceJourney departing 4 days ago with arrival 2 days ago should be INCLUDED`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                operatingDays = mutableListOf("opd1"),
                finalArrivalDayOffset = 2L  // Arrives 2 days after departure
            )
            repository.operatingDays["opd1"] = today.minusDays(4)  // Departed 4 days ago

            val calculator = ActiveDatesCalculator(repository = repository)
            val collector = ActiveEntitiesCollector()
            collector.addServiceJourney("sj1")
            collector.addOperatingDay("opd1")

            // With standard period starting 2 days ago, arrival at -2 days should be included
            val result = calculator.shouldIncludeDatedServiceJourney(
                "sj1",
                "opd1",
                collector,
                standardTimePeriod
            )
            assertTrue(result, "Journey arriving 2 days ago should be included when period starts 2 days ago")
        }

        @Test
        fun `DatedServiceJourney departing 5 days ago with arrival 3 days ago should be EXCLUDED`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                operatingDays = mutableListOf("opd1"),
                finalArrivalDayOffset = 2L
            )
            repository.operatingDays["opd1"] = today.minusDays(5)  // Departed 5 days ago, arrives 3 days ago

            val calculator = ActiveDatesCalculator(repository = repository)
            val collector = ActiveEntitiesCollector()
            collector.addServiceJourney("sj1")
            collector.addOperatingDay("opd1")

            val result = calculator.shouldIncludeDatedServiceJourney(
                "sj1",
                "opd1",
                collector,
                standardTimePeriod
            )
            assertFalse(result, "Journey arriving 3 days ago should be excluded when period starts 2 days ago")
        }
    }

    // ==================== PRESENT/FUTURE PROTECTION TESTS ====================

    @Nested
    inner class PresentAndFutureProtectionTests {

        @Test
        fun `ServiceJourney operating TODAY should be INCLUDED`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1")
            )
            repository.dayTypes["dt1"] = DayTypeData(
                dates = mutableListOf(today)
            )

            val calculator = ActiveDatesCalculator(repository = repository)
            val entityModel = TestDataFactory.defaultEntityModel()

            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, entityModel)

            assertTrue(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "ServiceJourney operating today must be included")
        }

        @Test
        fun `ServiceJourney operating TOMORROW should be INCLUDED`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1")
            )
            repository.dayTypes["dt1"] = DayTypeData(
                dates = mutableListOf(today.plusDays(1))
            )

            val calculator = ActiveDatesCalculator(repository = repository)
            val entityModel = TestDataFactory.defaultEntityModel()

            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, entityModel)

            assertTrue(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "ServiceJourney operating tomorrow must be included")
        }

        @Test
        fun `ServiceJourney operating in 6 MONTHS should be INCLUDED`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1")
            )
            repository.dayTypes["dt1"] = DayTypeData(
                dates = mutableListOf(today.plusMonths(6))
            )

            val calculator = ActiveDatesCalculator(repository = repository)
            val entityModel = TestDataFactory.defaultEntityModel()

            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, entityModel)

            assertTrue(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "ServiceJourney operating in 6 months must be included")
        }

        @Test
        fun `ServiceJourney operating YESTERDAY should be INCLUDED`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1")
            )
            repository.dayTypes["dt1"] = DayTypeData(
                dates = mutableListOf(today.minusDays(1))
            )

            val calculator = ActiveDatesCalculator(repository = repository)
            val entityModel = TestDataFactory.defaultEntityModel()

            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, entityModel)

            assertTrue(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "ServiceJourney operating yesterday must be included")
        }
    }

    // ==================== BOUNDARY CONDITION TESTS ====================

    @Nested
    inner class BoundaryConditionTests {

        @Test
        fun `ServiceJourney EXACTLY 2 days ago should be INCLUDED (boundary)`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1")
            )
            repository.dayTypes["dt1"] = DayTypeData(
                dates = mutableListOf(today.minusDays(2))
            )

            val calculator = ActiveDatesCalculator(repository = repository)
            val entityModel = TestDataFactory.defaultEntityModel()

            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, entityModel)

            assertTrue(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "ServiceJourney exactly 2 days ago must be included (boundary case)")
        }

        @Test
        fun `ServiceJourney 3 days ago should be EXCLUDED`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1")
            )
            repository.dayTypes["dt1"] = DayTypeData(
                dates = mutableListOf(today.minusDays(3))
            )

            val calculator = ActiveDatesCalculator(repository = repository)
            val entityModel = TestDataFactory.defaultEntityModel()

            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, entityModel)

            assertFalse(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "ServiceJourney 3 days ago must be excluded")
        }

        @Test
        fun `ServiceJourney 10 days ago should be EXCLUDED`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1")
            )
            repository.dayTypes["dt1"] = DayTypeData(
                dates = mutableListOf(today.minusDays(10))
            )

            val calculator = ActiveDatesCalculator(repository = repository)
            val entityModel = TestDataFactory.defaultEntityModel()

            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, entityModel)

            assertFalse(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "ServiceJourney 10 days ago must be excluded")
        }

        @Test
        fun `ServiceJourney EXACTLY at period end (1 year from now) should be INCLUDED`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1")
            )
            repository.dayTypes["dt1"] = DayTypeData(
                dates = mutableListOf(today.plusYears(1))
            )

            val calculator = ActiveDatesCalculator(repository = repository)
            val entityModel = TestDataFactory.defaultEntityModel()

            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, entityModel)

            assertTrue(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "ServiceJourney exactly at period end must be included")
        }
    }

    // ==================== SERVICE JOURNEY WITH DAYTYPE + OPERATING DAY TESTS ====================

    @Nested
    inner class ServiceJourneyWithDayTypeAndOperatingDayTests {

        @Test
        fun `ServiceJourney with DayType referencing OperatingDay TODAY should be INCLUDED`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1")
            )
            repository.dayTypes["dt1"] = DayTypeData(
                operatingDays = mutableListOf("opd1")
            )
            repository.operatingDays["opd1"] = today

            val calculator = ActiveDatesCalculator(repository = repository)
            val entityModel = TestDataFactory.defaultEntityModel()

            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, entityModel)

            assertTrue(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "ServiceJourney with DayType + OperatingDay today must be included")
            assertTrue(activeEntities["OperatingDay"]?.contains("opd1") == true,
                "OperatingDay must also be included")
        }

        @Test
        fun `ServiceJourney with DayType referencing OperatingDay 3 days ago should be EXCLUDED`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1")
            )
            repository.dayTypes["dt1"] = DayTypeData(
                operatingDays = mutableListOf("opd1")
            )
            repository.operatingDays["opd1"] = today.minusDays(3)

            val calculator = ActiveDatesCalculator(repository = repository)
            val entityModel = TestDataFactory.defaultEntityModel()

            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, entityModel)

            assertFalse(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "ServiceJourney with DayType + OperatingDay 3 days ago must be excluded")
        }

        @Test
        fun `ServiceJourney with arrival offset should use adjusted date for OperatingDay`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1"),
                finalArrivalDayOffset = 1L  // Arrives 1 day after operating day
            )
            repository.dayTypes["dt1"] = DayTypeData(
                operatingDays = mutableListOf("opd1")
            )
            // Operating day is 3 days ago, but with offset +1, arrival is 2 days ago (included)
            repository.operatingDays["opd1"] = today.minusDays(3)

            val calculator = ActiveDatesCalculator(repository = repository)
            val entityModel = TestDataFactory.defaultEntityModel()

            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, entityModel)

            assertTrue(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "ServiceJourney with operating day 3 days ago but arrival offset +1 should be included")
        }
    }

    // ==================== SERVICE JOURNEY WITH DIRECT OPERATING DAY TESTS ====================

    @Nested
    inner class ServiceJourneyWithDirectOperatingDayTests {

        @Test
        fun `ServiceJourney with direct OperatingDay reference TODAY should be INCLUDED`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                operatingDays = mutableListOf("opd1")
            )
            repository.operatingDays["opd1"] = today

            val calculator = ActiveDatesCalculator(repository = repository)
            val entityModel = TestDataFactory.defaultEntityModel()

            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, entityModel)

            assertTrue(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "ServiceJourney with direct OperatingDay today must be included")
        }

        @Test
        fun `ServiceJourney with direct OperatingDay 3 days ago should be EXCLUDED`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                operatingDays = mutableListOf("opd1")
            )
            repository.operatingDays["opd1"] = today.minusDays(3)

            val calculator = ActiveDatesCalculator(repository = repository)
            val entityModel = TestDataFactory.defaultEntityModel()

            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, entityModel)

            assertFalse(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "ServiceJourney with direct OperatingDay 3 days ago must be excluded")
        }

        @Test
        fun `ServiceJourney with direct OperatingDay and arrival offset should be properly adjusted`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                operatingDays = mutableListOf("opd1"),
                finalArrivalDayOffset = 2L
            )
            // Operating day is 4 days ago, arrival is 2 days ago (included)
            repository.operatingDays["opd1"] = today.minusDays(4)

            val calculator = ActiveDatesCalculator(repository = repository)
            val entityModel = TestDataFactory.defaultEntityModel()

            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, entityModel)

            assertTrue(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "ServiceJourney with arrival offset pushing into valid period should be included")
        }
    }

    // ==================== OPERATING PERIOD TESTS ====================

    @Nested
    inner class OperatingPeriodTests {

        @Test
        fun `ServiceJourney with OperatingPeriod fully in future should be INCLUDED`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1")
            )
            repository.dayTypes["dt1"] = DayTypeData(
                operatingPeriods = mutableListOf("op1")
            )
            repository.operatingPeriods["op1"] = OperatingPeriodData(
                period = Period(
                    fromDate = today.plusDays(10),
                    toDate = today.plusDays(20)
                )
            )

            val calculator = ActiveDatesCalculator(repository = repository)
            val entityModel = TestDataFactory.defaultEntityModel()

            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, entityModel)

            assertTrue(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "ServiceJourney with future OperatingPeriod must be included")
            assertTrue(activeEntities["OperatingPeriod"]?.contains("op1") == true,
                "OperatingPeriod must also be included")
        }

        @Test
        fun `ServiceJourney with OperatingPeriod fully in past (beyond threshold) should be EXCLUDED`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1")
            )
            repository.dayTypes["dt1"] = DayTypeData(
                operatingPeriods = mutableListOf("op1")
            )
            repository.operatingPeriods["op1"] = OperatingPeriodData(
                period = Period(
                    fromDate = today.minusDays(20),
                    toDate = today.minusDays(10)  // Ends 10 days ago
                )
            )

            val calculator = ActiveDatesCalculator(repository = repository)
            val entityModel = TestDataFactory.defaultEntityModel()

            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, entityModel)

            assertFalse(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "ServiceJourney with OperatingPeriod fully in past must be excluded")
        }

        @Test
        fun `ServiceJourney with OperatingPeriod straddling the threshold should be INCLUDED`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1")
            )
            repository.dayTypes["dt1"] = DayTypeData(
                operatingPeriods = mutableListOf("op1")
            )
            // Period from 5 days ago to 5 days in future - straddles threshold
            repository.operatingPeriods["op1"] = OperatingPeriodData(
                period = Period(
                    fromDate = today.minusDays(5),
                    toDate = today.plusDays(5)
                )
            )

            val calculator = ActiveDatesCalculator(repository = repository)
            val entityModel = TestDataFactory.defaultEntityModel()

            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, entityModel)

            assertTrue(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "ServiceJourney with OperatingPeriod straddling threshold must be included")
        }

        @Test
        fun `ServiceJourney with OperatingPeriod ending exactly at threshold should be INCLUDED`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1")
            )
            repository.dayTypes["dt1"] = DayTypeData(
                operatingPeriods = mutableListOf("op1")
            )
            // Period ends exactly 2 days ago (at threshold)
            repository.operatingPeriods["op1"] = OperatingPeriodData(
                period = Period(
                    fromDate = today.minusDays(10),
                    toDate = today.minusDays(2)
                )
            )

            val calculator = ActiveDatesCalculator(repository = repository)
            val entityModel = TestDataFactory.defaultEntityModel()

            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, entityModel)

            assertTrue(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "ServiceJourney with OperatingPeriod ending at threshold must be included")
        }

        @Test
        fun `ServiceJourney with OperatingPeriod ending 1 day before threshold should be EXCLUDED`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1")
            )
            repository.dayTypes["dt1"] = DayTypeData(
                operatingPeriods = mutableListOf("op1")
            )
            // Period ends 3 days ago (1 day before threshold)
            repository.operatingPeriods["op1"] = OperatingPeriodData(
                period = Period(
                    fromDate = today.minusDays(10),
                    toDate = today.minusDays(3)
                )
            )

            val calculator = ActiveDatesCalculator(repository = repository)
            val entityModel = TestDataFactory.defaultEntityModel()

            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, entityModel)

            assertFalse(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "ServiceJourney with OperatingPeriod ending before threshold must be excluded")
        }

        @Test
        fun `ServiceJourney with OperatingPeriod and arrival offset should adjust toDate`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1"),
                finalArrivalDayOffset = 2L  // Arrives 2 days after departure
            )
            repository.dayTypes["dt1"] = DayTypeData(
                operatingPeriods = mutableListOf("op1")
            )
            // Period ends 4 days ago, but with offset +2, effective end is 2 days ago (included)
            repository.operatingPeriods["op1"] = OperatingPeriodData(
                period = Period(
                    fromDate = today.minusDays(10),
                    toDate = today.minusDays(4)
                )
            )

            val calculator = ActiveDatesCalculator(repository = repository)
            val entityModel = TestDataFactory.defaultEntityModel()

            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, entityModel)

            assertTrue(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "ServiceJourney with arrival offset should adjust OperatingPeriod toDate")
        }

        @Test
        fun `OperatingPeriod with FromDateRef and ToDateRef should be resolved`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1")
            )
            repository.dayTypes["dt1"] = DayTypeData(
                operatingPeriods = mutableListOf("op1")
            )
            // OperatingPeriod references OperatingDays for from/to dates
            repository.operatingPeriods["op1"] = OperatingPeriodData(
                fromDateId = "opd_from",
                toDateId = "opd_to"
            )
            repository.operatingDays["opd_from"] = today
            repository.operatingDays["opd_to"] = today.plusDays(10)

            val calculator = ActiveDatesCalculator(repository = repository)
            val entityModel = TestDataFactory.defaultEntityModel()

            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, entityModel)

            assertTrue(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "ServiceJourney with OperatingPeriod using date references should work")
        }
    }

    // ==================== DAYS OF WEEK FILTERING TESTS ====================

    @Nested
    inner class DaysOfWeekFilteringTests {

        @Test
        fun `OperatingPeriod with DaysOfWeek filter should only consider matching days`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1")
            )

            // Find a Monday within our test range
            var nextMonday = today
            while (nextMonday.dayOfWeek != DayOfWeek.MONDAY) {
                nextMonday = nextMonday.plusDays(1)
            }

            repository.dayTypes["dt1"] = DayTypeData(
                operatingPeriods = mutableListOf("op1"),
                daysOfWeek = mutableSetOf(DayOfWeek.MONDAY)  // Only Mondays
            )
            // Period includes the next Monday
            repository.operatingPeriods["op1"] = OperatingPeriodData(
                period = Period(
                    fromDate = today,
                    toDate = today.plusDays(14)  // 2 weeks to ensure a Monday is included
                )
            )

            val calculator = ActiveDatesCalculator(repository = repository)
            val entityModel = TestDataFactory.defaultEntityModel()

            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, entityModel)

            assertTrue(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "ServiceJourney with OperatingPeriod containing matching DayOfWeek should be included")
        }

        @Test
        fun `OperatingPeriod with weekend-only filter for weekday-only period should be excluded`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1")
            )

            var nextMonday = today
            while (nextMonday.dayOfWeek != DayOfWeek.MONDAY) {
                nextMonday = nextMonday.plusDays(1)
            }

            repository.dayTypes["dt1"] = DayTypeData(
                operatingPeriods = mutableListOf("op1"),
                daysOfWeek = mutableSetOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
            )
            repository.operatingPeriods["op1"] = OperatingPeriodData(
                period = Period(
                    fromDate = nextMonday,
                    toDate = nextMonday.plusDays(4)
                )
            )

            val calculator = ActiveDatesCalculator(repository = repository)
            val entityModel = TestDataFactory.defaultEntityModel()

            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, entityModel)

            assertFalse(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "ServiceJourney should be excluded when DaysOfWeek has no intersection with the OperatingPeriod")
        }

        @Test
        fun `single-day OperatingPeriod on Monday with Saturday-only DayType should be excluded`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1")
            )

            var monday = today
            while (monday.dayOfWeek != DayOfWeek.MONDAY) {
                monday = monday.plusDays(1)
            }

            repository.dayTypes["dt1"] = DayTypeData(
                operatingPeriods = mutableListOf("op1"),
                daysOfWeek = mutableSetOf(DayOfWeek.SATURDAY)
            )
            repository.operatingPeriods["op1"] = OperatingPeriodData(
                period = Period(fromDate = monday, toDate = monday)
            )

            val calculator = ActiveDatesCalculator(repository = repository)
            val entityModel = TestDataFactory.defaultEntityModel()

            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, entityModel)

            assertFalse(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "ServiceJourney with single-day Monday period and Saturday-only DayType should be excluded")
            assertFalse(activeEntities["OperatingPeriod"]?.contains("op1") == true,
                "OperatingPeriod should not be retained when its only date is filtered out by DaysOfWeek")
        }

        @Test
        fun `OperatingPeriod with no DaysOfWeek restriction should be included`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1")
            )

            repository.dayTypes["dt1"] = DayTypeData(
                operatingPeriods = mutableListOf("op1"),
                daysOfWeek = mutableSetOf()
            )
            repository.operatingPeriods["op1"] = OperatingPeriodData(
                period = Period(fromDate = today, toDate = today.plusDays(6))
            )

            val calculator = ActiveDatesCalculator(repository = repository)
            val entityModel = TestDataFactory.defaultEntityModel()

            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, entityModel)

            assertTrue(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "ServiceJourney with no DaysOfWeek restriction should remain included")
        }
    }

    // ==================== IS AVAILABLE TESTS ====================

    @Nested
    inner class IsAvailableTests {

        @Test
        fun `literal-date cancellation drops the ServiceJourney when it is the only date`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1")
            )
            repository.dayTypes["dt1"] = DayTypeData(
                dates = mutableListOf(today),
                excludedDates = mutableSetOf(today),
            )

            val calculator = ActiveDatesCalculator(repository = repository)
            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, TestDataFactory.defaultEntityModel())

            assertFalse(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "SJ must be excluded when its only candidate date is cancelled by isAvailable=false")
        }

        @Test
        fun `single-day OperatingPeriod cancelled by a Date DTA drops the ServiceJourney`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1")
            )
            repository.dayTypes["dt1"] = DayTypeData(
                operatingPeriods = mutableListOf("op1"),
                excludedDates = mutableSetOf(today),
            )
            repository.operatingPeriods["op1"] = OperatingPeriodData(
                period = Period(fromDate = today, toDate = today)
            )

            val calculator = ActiveDatesCalculator(repository = repository)
            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, TestDataFactory.defaultEntityModel())

            assertFalse(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "SJ must be excluded when the only date in its OperatingPeriod is cancelled")
            assertFalse(activeEntities["OperatingPeriod"]?.contains("op1") == true,
                "OperatingPeriod must not be retained when fully cancelled for the only DayType referencing it")
        }

        @Test
        fun `OperatingDayRef cancellation drops the ServiceJourney`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1")
            )
            repository.dayTypes["dt1"] = DayTypeData(
                operatingDays = mutableListOf("od1"),
                excludedOperatingDays = mutableSetOf("od1"),
            )
            repository.operatingDays["od1"] = today

            val calculator = ActiveDatesCalculator(repository = repository)
            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, TestDataFactory.defaultEntityModel())

            assertFalse(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "SJ must be excluded when its only OperatingDay reference is cancelled")
        }

        @Test
        fun `OperatingPeriodRef cancellation drops the ServiceJourney`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1")
            )
            repository.dayTypes["dt1"] = DayTypeData(
                operatingPeriods = mutableListOf("op1"),
                excludedOperatingPeriods = mutableSetOf("op1"),
            )
            repository.operatingPeriods["op1"] = OperatingPeriodData(
                period = Period(fromDate = today, toDate = today.plusDays(7))
            )

            val calculator = ActiveDatesCalculator(repository = repository)
            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, TestDataFactory.defaultEntityModel())

            assertFalse(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "SJ must be excluded when its only OperatingPeriod reference is cancelled")
        }

        @Test
        fun `partial cancellation inside an OperatingPeriod keeps the ServiceJourney`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1")
            )

            var firstMonday = today
            while (firstMonday.dayOfWeek != DayOfWeek.MONDAY) {
                firstMonday = firstMonday.plusDays(1)
            }
            val secondMonday = firstMonday.plusDays(7)

            repository.dayTypes["dt1"] = DayTypeData(
                operatingPeriods = mutableListOf("op1"),
                daysOfWeek = mutableSetOf(DayOfWeek.MONDAY),
                excludedDates = mutableSetOf(firstMonday),
            )
            repository.operatingPeriods["op1"] = OperatingPeriodData(
                period = Period(fromDate = firstMonday, toDate = secondMonday.plusDays(6))
            )

            val calculator = ActiveDatesCalculator(repository = repository)
            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, TestDataFactory.defaultEntityModel())

            assertTrue(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "SJ must remain included when at least one Monday in the period survives the cancellation")
        }

        @Test
        fun `cancellation DTA is retained in the output when its DayType has other active dates`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1")
            )

            val activeDate = today
            val cancelledDate = today.plusDays(1)
            val cancellationDtaId = "TST:DayTypeAssignment:cancel"

            repository.dayTypes["dt1"] = DayTypeData(
                dates = mutableListOf(activeDate),
                excludedDates = mutableSetOf(cancelledDate),
            )
            repository.addDayTypeAssignmentForExcludedDate("dt1", cancelledDate, cancellationDtaId)

            val calculator = ActiveDatesCalculator(repository = repository)
            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, TestDataFactory.defaultEntityModel())

            assertTrue(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "SJ must be included via the additive active date")
            assertTrue(activeEntities["DayTypeAssignment"]?.contains(cancellationDtaId) == true,
                "Cancellation DTA must be retained so the output preserves the cancellation")
        }

        @Test
        fun `cancellation DTA is NOT retained when its DayType has no active dates`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1")
            )

            val cancelledDate = today
            val cancellationDtaId = "TST:DayTypeAssignment:cancel"

            repository.dayTypes["dt1"] = DayTypeData(
                excludedDates = mutableSetOf(cancelledDate),
            )
            repository.addDayTypeAssignmentForExcludedDate("dt1", cancelledDate, cancellationDtaId)

            val calculator = ActiveDatesCalculator(repository = repository)
            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, TestDataFactory.defaultEntityModel())

            assertFalse(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "SJ must be excluded — DayType has only a cancellation, no active dates")
            assertFalse(activeEntities["DayTypeAssignment"]?.contains(cancellationDtaId) == true,
                "Cancellation DTA must not be retained when its DayType contributes no active date")
        }
    }

    // ==================== DAYTYPE WITHOUT PROPERTIES TESTS ====================

    @Nested
    inner class DayTypeWithoutPropertiesTests {

        @Test
        fun `DayType with OperatingPeriod but no DaysOfWeek should include ServiceJourney`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1")
            )
            repository.dayTypes["dt1"] = DayTypeData(
                operatingPeriods = mutableListOf("op1")
                // No daysOfWeek set — DayType has no PropertyOfDay
            )
            repository.operatingPeriods["op1"] = OperatingPeriodData(
                period = Period(
                    fromDate = today,
                    toDate = today.plusDays(14)
                )
            )

            val calculator = ActiveDatesCalculator(repository = repository)
            val entityModel = TestDataFactory.defaultEntityModel()

            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, entityModel)

            assertTrue(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "DayType with OperatingPeriod but no DaysOfWeek should treat all days as valid")
        }

        @Test
        fun `DayType with OperatingDay but no DaysOfWeek should include ServiceJourney`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1")
            )
            repository.dayTypes["dt1"] = DayTypeData(
                operatingDays = mutableListOf("opd1")
                // No daysOfWeek set — DayType has no PropertyOfDay
            )
            repository.operatingDays["opd1"] = today

            val calculator = ActiveDatesCalculator(repository = repository)
            val entityModel = TestDataFactory.defaultEntityModel()

            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, entityModel)

            assertTrue(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "DayType with OperatingDay but no DaysOfWeek should include ServiceJourney")
        }

        @Test
        fun `DayType with Date but no DaysOfWeek should include ServiceJourney`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1")
            )
            repository.dayTypes["dt1"] = DayTypeData(
                dates = mutableListOf(today.plusDays(5))
                // No daysOfWeek set — DayType has no PropertyOfDay
            )

            val calculator = ActiveDatesCalculator(repository = repository)
            val entityModel = TestDataFactory.defaultEntityModel()

            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, entityModel)

            assertTrue(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "DayType with Date but no DaysOfWeek should include ServiceJourney")
        }

        @Test
        fun `DayType with multiple OperatingPeriods but no DaysOfWeek should include ServiceJourney`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1")
            )
            repository.dayTypes["dt1"] = DayTypeData(
                operatingPeriods = mutableListOf("op1", "op2")
                // No daysOfWeek set — DayType has no PropertyOfDay
            )
            repository.operatingPeriods["op1"] = OperatingPeriodData(
                period = Period(
                    fromDate = today,
                    toDate = today.plusDays(14)
                )
            )
            repository.operatingPeriods["op2"] = OperatingPeriodData(
                period = Period(
                    fromDate = today.plusDays(30),
                    toDate = today.plusDays(60)
                )
            )

            val calculator = ActiveDatesCalculator(repository = repository)
            val entityModel = TestDataFactory.defaultEntityModel()

            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, entityModel)

            assertTrue(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "DayType with multiple OperatingPeriods but no DaysOfWeek should include ServiceJourney")
            assertTrue(activeEntities["OperatingPeriod"]?.contains("op1") == true,
                "First OperatingPeriod should be included")
            assertTrue(activeEntities["OperatingPeriod"]?.contains("op2") == true,
                "Second OperatingPeriod should be included")
        }

        @Test
        fun `DayType with multiple OperatingDays but no DaysOfWeek should include ServiceJourney`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1")
            )
            repository.dayTypes["dt1"] = DayTypeData(
                operatingDays = mutableListOf("opd1", "opd2")
                // No daysOfWeek set — DayType has no PropertyOfDay
            )
            repository.operatingDays["opd1"] = today
            repository.operatingDays["opd2"] = today.plusDays(7)

            val calculator = ActiveDatesCalculator(repository = repository)
            val entityModel = TestDataFactory.defaultEntityModel()

            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, entityModel)

            assertTrue(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "DayType with multiple OperatingDays but no DaysOfWeek should include ServiceJourney")
            assertTrue(activeEntities["OperatingDay"]?.contains("opd1") == true,
                "First OperatingDay should be included")
            assertTrue(activeEntities["OperatingDay"]?.contains("opd2") == true,
                "Second OperatingDay should be included")
        }

        @Test
        fun `DayType with multiple Dates but no DaysOfWeek should include ServiceJourney`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1")
            )
            repository.dayTypes["dt1"] = DayTypeData(
                dates = mutableListOf(today.plusDays(5), today.plusDays(10))
                // No daysOfWeek set — DayType has no PropertyOfDay
            )

            val calculator = ActiveDatesCalculator(repository = repository)
            val entityModel = TestDataFactory.defaultEntityModel()

            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, entityModel)

            assertTrue(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "DayType with multiple Dates but no DaysOfWeek should include ServiceJourney")
        }

        @Test
        fun `DayType with both OperatingPeriod and Date but no DaysOfWeek should include ServiceJourney`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1")
            )
            repository.dayTypes["dt1"] = DayTypeData(
                operatingPeriods = mutableListOf("op1"),
                dates = mutableListOf(today.plusDays(5))
                // No daysOfWeek set — DayType has no PropertyOfDay
            )
            repository.operatingPeriods["op1"] = OperatingPeriodData(
                period = Period(
                    fromDate = today,
                    toDate = today.plusDays(14)
                )
            )

            val calculator = ActiveDatesCalculator(repository = repository)
            val entityModel = TestDataFactory.defaultEntityModel()

            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, entityModel)

            assertTrue(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "DayType with both OperatingPeriod and Date but no DaysOfWeek should include ServiceJourney")
            assertTrue(activeEntities["OperatingPeriod"]?.contains("op1") == true,
                "OperatingPeriod should also be included")
        }

        @Test
        fun `DayType with expired OperatingPeriod but valid Date should still include ServiceJourney`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1")
            )
            repository.dayTypes["dt1"] = DayTypeData(
                operatingPeriods = mutableListOf("op1"),
                dates = mutableListOf(today.plusDays(5))
                // No daysOfWeek set — DayType has no PropertyOfDay
            )
            repository.operatingPeriods["op1"] = OperatingPeriodData(
                period = Period(
                    fromDate = today.minusDays(30),
                    toDate = today.minusDays(20) // Fully expired
                )
            )

            val calculator = ActiveDatesCalculator(repository = repository)
            val entityModel = TestDataFactory.defaultEntityModel()

            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, entityModel)

            assertTrue(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "ServiceJourney should be included if Date is valid even when OperatingPeriod is expired")
        }

        @Test
        fun `DayType with valid OperatingPeriod but expired Date should still include ServiceJourney`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1")
            )
            repository.dayTypes["dt1"] = DayTypeData(
                operatingPeriods = mutableListOf("op1"),
                dates = mutableListOf(today.minusDays(30)) // Expired date
                // No daysOfWeek set — DayType has no PropertyOfDay
            )
            repository.operatingPeriods["op1"] = OperatingPeriodData(
                period = Period(
                    fromDate = today,
                    toDate = today.plusDays(14)
                )
            )

            val calculator = ActiveDatesCalculator(repository = repository)
            val entityModel = TestDataFactory.defaultEntityModel()

            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, entityModel)

            assertTrue(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "ServiceJourney should be included if OperatingPeriod is valid even when Date is expired")
        }
    }

    // ==================== MULTIPLE DATES/PERIODS TESTS ====================

    @Nested
    inner class MultipleDatesAndPeriodsTests {

        @Test
        fun `ServiceJourney with multiple dates where at least one is valid should be INCLUDED`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1")
            )
            repository.dayTypes["dt1"] = DayTypeData(
                dates = mutableListOf(
                    today.minusDays(10),  // Invalid (too old)
                    today.minusDays(5),   // Invalid (too old)
                    today                  // Valid (today)
                )
            )

            val calculator = ActiveDatesCalculator(repository = repository)
            val entityModel = TestDataFactory.defaultEntityModel()

            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, entityModel)

            assertTrue(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "ServiceJourney with at least one valid date should be included")
        }

        @Test
        fun `ServiceJourney with multiple DayTypes where at least one is valid should be INCLUDED`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1", "dt2")
            )
            repository.dayTypes["dt1"] = DayTypeData(
                dates = mutableListOf(today.minusDays(10))  // Invalid
            )
            repository.dayTypes["dt2"] = DayTypeData(
                dates = mutableListOf(today.plusDays(5))  // Valid
            )

            val calculator = ActiveDatesCalculator(repository = repository)
            val entityModel = TestDataFactory.defaultEntityModel()

            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, entityModel)

            assertTrue(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "ServiceJourney with at least one valid DayType should be included")
        }

        @Test
        fun `ServiceJourney with all invalid dates should be EXCLUDED`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1")
            )
            repository.dayTypes["dt1"] = DayTypeData(
                dates = mutableListOf(
                    today.minusDays(10),
                    today.minusDays(20),
                    today.minusDays(30)
                )
            )

            val calculator = ActiveDatesCalculator(repository = repository)
            val entityModel = TestDataFactory.defaultEntityModel()

            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, entityModel)

            assertFalse(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "ServiceJourney with all invalid dates should be excluded")
        }
    }

    // ==================== EDGE CASE TESTS ====================

    @Nested
    inner class EdgeCaseTests {

        @Test
        fun `ServiceJourney with no dates or periods should be EXCLUDED`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1")
            )
            repository.dayTypes["dt1"] = DayTypeData()  // Empty - no dates, periods, or operating days

            val calculator = ActiveDatesCalculator(repository = repository)
            val entityModel = TestDataFactory.defaultEntityModel()

            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, entityModel)

            assertFalse(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "ServiceJourney with empty DayType should be excluded")
        }

        @Test
        fun `ServiceJourney referencing non-existent DayType should be EXCLUDED`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("nonexistent_dt")
            )
            // DayType not added to repository

            val calculator = ActiveDatesCalculator(repository = repository)
            val entityModel = TestDataFactory.defaultEntityModel()

            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, entityModel)

            assertFalse(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "ServiceJourney with non-existent DayType should be excluded")
        }

        @Test
        fun `ServiceJourney referencing non-existent OperatingDay should be EXCLUDED`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                operatingDays = mutableListOf("nonexistent_opd")
            )
            // OperatingDay not added to repository

            val calculator = ActiveDatesCalculator(repository = repository)
            val entityModel = TestDataFactory.defaultEntityModel()

            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, entityModel)

            assertFalse(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "ServiceJourney with non-existent OperatingDay should be excluded")
        }

        @Test
        fun `Zero arrival offset should not affect date calculation`() {
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1"),
                finalArrivalDayOffset = 0L  // Explicit zero offset
            )
            repository.dayTypes["dt1"] = DayTypeData(
                dates = mutableListOf(today.minusDays(2))  // Exactly at threshold
            )

            val calculator = ActiveDatesCalculator(repository = repository)
            val entityModel = TestDataFactory.defaultEntityModel()

            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, entityModel)

            assertTrue(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "ServiceJourney with zero offset at threshold should be included")
        }

        @Test
        fun `Negative arrival offset should correctly exclude journey`() {
            // Edge case: arrival before departure (shouldn't happen in real data, but test the math)
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1"),
                finalArrivalDayOffset = -1L  // Arrives 1 day before operating day (unusual)
            )
            repository.dayTypes["dt1"] = DayTypeData(
                dates = mutableListOf(today.minusDays(1))  // Operating day is yesterday
            )
            // With -1 offset, adjusted date is 2 days ago, which is exactly at threshold

            val calculator = ActiveDatesCalculator(repository = repository)
            val entityModel = TestDataFactory.defaultEntityModel()

            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, entityModel)

            assertTrue(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "Negative offset should correctly calculate adjusted date")
        }
    }

    // ==================== DAY TYPE ASSIGNMENT LITERAL DATE + ARRIVAL OFFSET TESTS ====================

    @Nested
    inner class DayTypeAssignmentLiteralDateWithArrivalOffsetTests {

        private fun seedDtaLiteralDate(
            repository: ActiveDatesRepository,
            dayTypeId: String,
            date: java.time.LocalDate,
            dayTypeAssignmentId: String,
        ) {
            repository.getDayTypeData(dayTypeId).dates.add(date)
            repository.dayTypeAssignmentToDate[dayTypeAssignmentId] = date
            repository.addDayTypeAssignmentForDate(dayTypeId, date, dayTypeAssignmentId)
        }

        @Test
        fun `DTA with literal Date 3 days ago and ServiceJourney with arrival offset 2 should INCLUDE DTA`() {
            // Regression guard for the reported bug.
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1"),
                finalArrivalDayOffset = 2L
            )
            seedDtaLiteralDate(repository, "dt1", today.minusDays(3), "dta1")

            val calculator = ActiveDatesCalculator(repository = repository)
            val entityModel = TestDataFactory.defaultEntityModel()

            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, entityModel)

            assertTrue(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "ServiceJourney with offset +2 over a date 3 days ago must be included")
            assertTrue(activeEntities["DayTypeAssignment"]?.contains("dta1") == true,
                "DayTypeAssignment supplying that date must be included (offset applied)")
        }

        @Test
        fun `DTA with literal Date 3 days ago and ServiceJourney with zero offset should EXCLUDE DTA`() {
            // Negative case — confirms the fix doesn't over-include.
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1"),
                finalArrivalDayOffset = 0L
            )
            seedDtaLiteralDate(repository, "dt1", today.minusDays(3), "dta1")

            val calculator = ActiveDatesCalculator(repository = repository)
            val entityModel = TestDataFactory.defaultEntityModel()

            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, entityModel)

            assertFalse(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "ServiceJourney with no offset over a date before window must be excluded")
            assertFalse(activeEntities["DayTypeAssignment"]?.contains("dta1") == true,
                "DayTypeAssignment supplying an out-of-window date must be excluded when no journey offsets it in")
        }

        @Test
        fun `DTA with literal Date 3 days ago and DeadRun with arrival offset 2 should INCLUDE DTA`() {
            val repository = ActiveDatesRepository()
            repository.deadRuns["dr1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1"),
                finalArrivalDayOffset = 2L
            )
            seedDtaLiteralDate(repository, "dt1", today.minusDays(3), "dta1")

            val calculator = ActiveDatesCalculator(repository = repository)
            val entityModel = TestDataFactory.defaultEntityModel()

            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, entityModel)

            assertTrue(activeEntities["DeadRun"]?.contains("dr1") == true,
                "DeadRun with offset +2 over a date 3 days ago must be included")
            assertTrue(activeEntities["DayTypeAssignment"]?.contains("dta1") == true,
                "DayTypeAssignment supplying that date must be included via the DeadRun's offset")
        }

        @Test
        fun `DTA shared by two ServiceJourneys, only the offset journey activates it, DTA should be INCLUDED`() {
            // Two journeys reference the same DayType; max offset wins.
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj_zero"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1"),
                finalArrivalDayOffset = 0L
            )
            repository.serviceJourneys["sj_offset"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1"),
                finalArrivalDayOffset = 2L
            )
            seedDtaLiteralDate(repository, "dt1", today.minusDays(3), "dta1")

            val calculator = ActiveDatesCalculator(repository = repository)
            val entityModel = TestDataFactory.defaultEntityModel()

            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, entityModel)

            assertTrue(activeEntities["ServiceJourney"]?.contains("sj_offset") == true,
                "The +2 offset ServiceJourney must be active")
            assertFalse(activeEntities["ServiceJourney"]?.contains("sj_zero") == true,
                "The zero-offset ServiceJourney must not be active for an out-of-window date")
            assertTrue(activeEntities["DayTypeAssignment"]?.contains("dta1") == true,
                "DayTypeAssignment must be included because at least one referencing journey's offset puts the date in window")
        }

        @Test
        fun `DTA with literal Date beyond the end of the window should be EXCLUDED regardless of offset`() {
            // End-of-window check uses the raw date — offset cannot rescue a far-future date.
            val repository = ActiveDatesRepository()
            repository.serviceJourneys["sj1"] = VehicleJourneyData(
                dayTypes = mutableListOf("dt1"),
                finalArrivalDayOffset = 5L
            )
            seedDtaLiteralDate(repository, "dt1", today.plusYears(5), "dta1")

            val calculator = ActiveDatesCalculator(repository = repository)
            val entityModel = TestDataFactory.defaultEntityModel()

            val activeEntities = calculator.activeDateEntitiesInPeriod(standardTimePeriod, entityModel)

            assertFalse(activeEntities["ServiceJourney"]?.contains("sj1") == true,
                "ServiceJourney with date beyond +3y window must be excluded even with a +5 offset")
            assertFalse(activeEntities["DayTypeAssignment"]?.contains("dta1") == true,
                "DayTypeAssignment with date beyond +3y window must be excluded")
        }
    }
}
