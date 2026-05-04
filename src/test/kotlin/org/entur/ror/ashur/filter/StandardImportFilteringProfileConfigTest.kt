package org.entur.ror.ashur.filter

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class StandardImportFilteringProfileConfigTest {

    private fun standardConfig() = StandardImportFilteringProfileConfig().build(
        FilterContext(profile = FilterProfile.StandardImportFilter, codespace = "TST")
    )

    @Test
    fun testBuildReturnsNonNullConfig() {
        assertNotNull(standardConfig())
    }

    @Test
    fun testSkipsVehicleScheduleFrame() {
        val config = standardConfig()

        assertTrue(
            config.skipElements.containsAll(
                listOf(
                    "/PublicationDelivery/dataObjects/CompositeFrame/frames/VehicleScheduleFrame",
                    "/PublicationDelivery/dataObjects/VehicleScheduleFrame"
                )
            )
        )
    }

    @Test
    fun testRemovesPrivateData() {
        assertTrue(standardConfig().removePrivateData)
    }

    @Test
    fun testHasSharedEntitySelectors() {
        val selectorClassNames = standardConfig().entitySelectors.map { it.javaClass.simpleName }

        assertTrue(selectorClassNames.contains("ActiveDatesSelector"))
        assertTrue(selectorClassNames.contains("PassengerStopAssignmentSelector"))
        assertTrue(selectorClassNames.contains("ServiceJourneyInterchangeSelector"))
    }

    @Test
    fun testDoesNotHaveBlockSelector() {
        val selectorClassNames = standardConfig().entitySelectors.map { it.javaClass.simpleName }

        assertFalse(
            selectorClassNames.contains("BlockSelector"),
            "StandardImportFilteringProfileConfig should not register a BlockSelector"
        )
    }

    @Test
    fun testSkipsPrivateContactDetailsAndDescription() {
        val config = standardConfig()

        assertTrue(
            config.skipElements.containsAll(
                listOf(
                    "/PublicationDelivery/dataObjects/ResourceFrame/organisations/Operator/ContactDetails/Email",
                    "/PublicationDelivery/dataObjects/CompositeFrame/frames/ResourceFrame/organisations/Operator/ContactDetails/Email",
                    "/PublicationDelivery/dataObjects/ResourceFrame/organisations/Authority/ContactDetails/Email",
                    "/PublicationDelivery/dataObjects/CompositeFrame/frames/ResourceFrame/organisations/Authority/ContactDetails/Email",
                    "/PublicationDelivery/dataObjects/CompositeFrame/codespaces/Codespace/Description"
                )
            )
        )
    }

    @Test
    fun testSkipsSiteAndInfrastructureFrames() {
        val config = standardConfig()

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
    fun testSkipsDeadRuns() {
        val config = standardConfig()

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
        val config = standardConfig()

        assertFalse(config.preserveComments)
        assertTrue(config.pruneReferences)
        assertTrue(config.useSelfClosingTagsWhereApplicable)
        assertTrue(config.referencesToExcludeFromPruning.contains("QuayRef"))
    }
}
