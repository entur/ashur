package org.entur.ror.ashur.filter

import org.entur.netex.tools.lib.config.TimePeriod
import org.entur.netex.tools.lib.selectors.entities.EntitySelector
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class BaseFilteringProfileConfigTest {

    private fun standardContext() =
        FilterContext(profile = FilterProfile.StandardImportFilter, codespace = "TST")

    private fun standardConfig() = StandardImportFilteringProfileConfig().build(standardContext())

    @Test
    fun testSharedSkipElements() {
        val config = standardConfig()

        assertTrue(
            config.skipElements.containsAll(
                listOf(
                    "/PublicationDelivery/dataObjects/ResourceFrame/organisations/Operator/ContactDetails/Email",
                    "/PublicationDelivery/dataObjects/CompositeFrame/frames/ResourceFrame/organisations/Operator/ContactDetails/Email",
                    "/PublicationDelivery/dataObjects/ResourceFrame/organisations/Authority/ContactDetails/Email",
                    "/PublicationDelivery/dataObjects/CompositeFrame/frames/ResourceFrame/organisations/Authority/ContactDetails/Email",
                    "/PublicationDelivery/dataObjects/CompositeFrame/codespaces/Codespace/Description",
                    "/PublicationDelivery/dataObjects/CompositeFrame/frames/InfrastructureFrame",
                    "/PublicationDelivery/dataObjects/InfrastructureFrame",
                    "/PublicationDelivery/dataObjects/CompositeFrame/FrameDefaults",
                    "/PublicationDelivery/dataObjects/CompositeFrame/frames/ServiceFrame/lines/Line/routes",
                    "/PublicationDelivery/dataObjects/ServiceFrame/lines/Line/routes",
                    "/PublicationDelivery/dataObjects/CompositeFrame/frames/TimetableFrame/vehicleJourneys/DeadRun",
                    "/PublicationDelivery/dataObjects/TimetableFrame/vehicleJourneys/DeadRun",
                    "/PublicationDelivery/dataObjects/CompositeFrame/frames/TimetableFrame/trainNumbers",
                    "/PublicationDelivery/dataObjects/TimetableFrame/trainNumbers",
                    "/PublicationDelivery/dataObjects/CompositeFrame/frames/TimetableFrame/serviceFacilitySets",
                    "/PublicationDelivery/dataObjects/TimetableFrame/serviceFacilitySets",
                    "/PublicationDelivery/dataObjects/CompositeFrame/frames/ResourceFrame/dataSources",
                    "/PublicationDelivery/dataObjects/ResourceFrame/dataSources",
                    "/PublicationDelivery/dataObjects/CompositeFrame/frames/ResourceFrame/vehicleTypes",
                    "/PublicationDelivery/dataObjects/ResourceFrame/vehicleTypes",
                    "/PublicationDelivery/dataObjects/CompositeFrame/frames/TimetableFrame/vehicleJourneys/ServiceJourney/parts",
                    "/PublicationDelivery/dataObjects/TimetableFrame/vehicleJourneys/ServiceJourney/parts",
                    "/PublicationDelivery/dataObjects/CompositeFrame/frames/ResourceFrame/vehicles",
                    "/PublicationDelivery/dataObjects/ResourceFrame/vehicles",
                    "/PublicationDelivery/dataObjects/CompositeFrame/frames/SiteFrame",
                    "/PublicationDelivery/dataObjects/SiteFrame"
                )
            )
        )
    }

    @Test
    fun testSharedBuilderSettings() {
        val config = standardConfig()

        assertFalse(config.preserveComments)
        assertTrue(config.pruneReferences)
        assertTrue(config.referencesToExcludeFromPruning.contains("QuayRef"))
        assertTrue(config.useSelfClosingTagsWhereApplicable)
    }

    @Test
    fun testSharedUnreferencedEntitiesToPrune() {
        val config = standardConfig()

        assertTrue(
            config.unreferencedEntitiesToPrune.containsAll(
                listOf(
                    "JourneyPattern",
                    "Route",
                    "Line",
                    "Operator",
                    "Notice",
                    "DestinationDisplay",
                    "ServiceLink",
                    "ScheduledStopPoint",
                    "RoutePoint",
                    "DayType",
                    "OperatingPeriod",
                    "OperatingDay"
                )
            )
        )
    }

    @Test
    fun testSharedRequiredChildren() {
        val config = standardConfig()

        assertTrue(config.elementsRequiredChildren.containsKey("NoticeAssignment"))
        assertTrue(
            config.elementsRequiredChildren["NoticeAssignment"]!!
                .containsAll(listOf("NoticeRef", "NoticedObjectRef"))
        )
    }

    @Test
    fun testSharedEntitySelectors() {
        val config = standardConfig()

        val selectorClassNames = config.entitySelectors.map { it.javaClass.simpleName }
        assertTrue(selectorClassNames.contains("ActiveDatesSelector"))
        assertTrue(selectorClassNames.contains("PassengerStopAssignmentSelector"))
        assertTrue(selectorClassNames.contains("ServiceJourneyInterchangeSelector"))
    }

    @Test
    fun testStandardTimePeriodStartsTwoDaysBeforeToday() {
        val period = BaseFilteringProfileConfig.standardTimePeriod()

        assertEquals(LocalDate.now().minusDays(2), period.start)
    }

    @Test
    fun testStandardTimePeriodEndsThreeYearsAfterToday() {
        val period = BaseFilteringProfileConfig.standardTimePeriod()

        assertEquals(LocalDate.now().plusYears(3), period.end)
    }

    @Test
    fun testIncludeElementsHookRemovesPathsFromSkipElements() {
        val included = listOf(
            "/PublicationDelivery/dataObjects/CompositeFrame/frames/SiteFrame",
            "/PublicationDelivery/dataObjects/SiteFrame"
        )
        val config = TestableBaseFilteringProfileConfig(includeElementsHook = included)
            .build(standardContext())

        included.forEach { path ->
            assertFalse(
                config.skipElements.contains(path),
                "Expected path '$path' to be excluded from skipElements when listed in includeElements()"
            )
        }
        // Sanity check: an unrelated path should still be skipped
        assertTrue(
            config.skipElements.contains(
                "/PublicationDelivery/dataObjects/CompositeFrame/frames/InfrastructureFrame"
            )
        )
    }

    @Test
    fun testAdditionalEntitySelectorsHookIsAppendedAfterShared() {
        val extraSelector = NoOpEntitySelector()
        val config = TestableBaseFilteringProfileConfig(additionalEntitySelectorsHook = listOf(extraSelector))
            .build(standardContext())

        assertTrue(config.entitySelectors.contains(extraSelector))
        // Shared selectors must still be present
        val selectorClassNames = config.entitySelectors.map { it.javaClass.simpleName }
        assertTrue(selectorClassNames.contains("ActiveDatesSelector"))
        assertTrue(selectorClassNames.contains("PassengerStopAssignmentSelector"))
        assertTrue(selectorClassNames.contains("ServiceJourneyInterchangeSelector"))
    }

    @Test
    fun testAdditionalElementsRequiredChildrenHookIsMergedWithDefaults() {
        val extra = mapOf("CustomElement" to listOf("ChildA", "ChildB"))
        val config = TestableBaseFilteringProfileConfig(additionalRequiredChildrenHook = extra)
            .build(standardContext())

        assertTrue(config.elementsRequiredChildren.containsKey("NoticeAssignment"))
        assertTrue(config.elementsRequiredChildren.containsKey("CustomElement"))
        assertEquals(listOf("ChildA", "ChildB"), config.elementsRequiredChildren["CustomElement"])
    }

    @Test
    fun testRemovePrivateDataIsPropagatedFromSubclass() {
        val configRemoves = TestableBaseFilteringProfileConfig(removePrivateDataValue = true)
            .build(standardContext())
        val configKeeps = TestableBaseFilteringProfileConfig(removePrivateDataValue = false)
            .build(standardContext())

        assertTrue(configRemoves.removePrivateData)
        assertFalse(configKeeps.removePrivateData)
    }

    @Test
    fun testBuildReturnsNonNullConfig() {
        assertNotNull(standardConfig())
    }

    /**
     * Test subclass exposing the hook methods of [BaseFilteringProfileConfig] so the hook
     * contract can be verified without coupling to a production subclass.
     */
    private class TestableBaseFilteringProfileConfig(
        private val removePrivateDataValue: Boolean = false,
        private val includeElementsHook: List<String> = emptyList(),
        private val additionalEntitySelectorsHook: List<EntitySelector> = emptyList(),
        private val additionalRequiredChildrenHook: Map<String, List<String>> = emptyMap()
    ) : BaseFilteringProfileConfig() {
        override val removePrivateData: Boolean
            get() = removePrivateDataValue

        override fun includeElements(): List<String> = includeElementsHook

        override fun additionalEntitySelectors(
            activeDatesRepository: ActiveDatesRepository,
            timePeriod: TimePeriod
        ): List<EntitySelector> = additionalEntitySelectorsHook

        override fun additionalElementsRequiredChildren(): Map<String, List<String>> =
            additionalRequiredChildrenHook
    }

    private class NoOpEntitySelector : EntitySelector {
        override fun selectEntities(
            context: org.entur.netex.tools.lib.selectors.entities.EntitySelectorContext
        ) = context.currentEntitySelection!!
    }
}
