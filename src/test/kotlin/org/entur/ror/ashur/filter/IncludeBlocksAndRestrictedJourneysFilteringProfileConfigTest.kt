package org.entur.ror.ashur.filter

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class IncludeBlocksAndRestrictedJourneysFilteringProfileConfigTest {

    private fun includeBlocksConfig() = IncludeBlocksAndRestrictedJourneysFilteringProfileConfig().build(
        FilterContext(profile = FilterProfile.IncludeBlocksAndRestrictedJourneysFilter, codespace = "TST")
    )

    @Test
    fun testBuildReturnsNonNullConfig() {
        assertNotNull(includeBlocksConfig())
    }

    @Test
    fun testDoesNotSkipVehicleScheduleFrame() {
        val config = includeBlocksConfig()

        assertFalse(
            config.skipElements.contains(
                "/PublicationDelivery/dataObjects/CompositeFrame/frames/VehicleScheduleFrame"
            )
        )
        assertFalse(
            config.skipElements.contains(
                "/PublicationDelivery/dataObjects/VehicleScheduleFrame"
            )
        )
    }

    @Test
    fun testKeepsPrivateData() {
        assertFalse(includeBlocksConfig().removePrivateData)
    }

    @Test
    fun testHasBlockSelector() {
        val selectorClassNames = includeBlocksConfig().entitySelectors.map { it.javaClass.simpleName }

        assertTrue(selectorClassNames.contains("BlockSelector"))
    }

    @Test
    fun testHasSharedEntitySelectors() {
        val selectorClassNames = includeBlocksConfig().entitySelectors.map { it.javaClass.simpleName }

        assertTrue(selectorClassNames.contains("ActiveDatesSelector"))
        assertTrue(selectorClassNames.contains("PassengerStopAssignmentSelector"))
        assertTrue(selectorClassNames.contains("ServiceJourneyInterchangeSelector"))
    }

    @Test
    fun testStillSkipsSiteAndInfrastructureFrames() {
        val config = includeBlocksConfig()

        assertTrue(
            config.skipElements.containsAll(
                listOf(
                    "/PublicationDelivery/dataObjects/CompositeFrame/frames/SiteFrame",
                    "/PublicationDelivery/dataObjects/SiteFrame",
                    "/PublicationDelivery/dataObjects/CompositeFrame/frames/InfrastructureFrame",
                    "/PublicationDelivery/dataObjects/InfrastructureFrame"
                )
            )
        )
    }

    @Test
    fun testStillSkipsDeadRuns() {
        val config = includeBlocksConfig()

        assertTrue(
            config.skipElements.containsAll(
                listOf(
                    "/PublicationDelivery/dataObjects/CompositeFrame/frames/TimetableFrame/vehicleJourneys/DeadRun",
                    "/PublicationDelivery/dataObjects/TimetableFrame/vehicleJourneys/DeadRun"
                )
            )
        )
    }

    @Test
    fun testHasStandardBuilderSettings() {
        val config = includeBlocksConfig()

        assertFalse(config.preserveComments)
        assertTrue(config.pruneReferences)
        assertTrue(config.useSelfClosingTagsWhereApplicable)
        assertTrue(config.referencesToExcludeFromPruning.contains("QuayRef"))
    }

    @Test
    fun testInheritsSharedUnreferencedEntitiesToPrune() {
        val config = includeBlocksConfig()

        assertTrue(
            config.unreferencedEntitiesToPrune.containsAll(
                listOf(
                    "JourneyPattern",
                    "Route",
                    "Line",
                    "Operator",
                    "DayType",
                    "OperatingPeriod",
                    "OperatingDay"
                )
            )
        )
    }

    @Test
    fun testInheritsNoticeAssignmentRequiredChildren() {
        val config = includeBlocksConfig()

        assertTrue(config.elementsRequiredChildren.containsKey("NoticeAssignment"))
        assertTrue(
            config.elementsRequiredChildren["NoticeAssignment"]!!
                .containsAll(listOf("NoticeRef", "NoticedObjectRef"))
        )
    }
}
