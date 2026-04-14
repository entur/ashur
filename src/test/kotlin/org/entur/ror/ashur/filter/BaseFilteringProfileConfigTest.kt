package org.entur.ror.ashur.filter

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BaseFilteringProfileConfigTest {

    @Test
    fun testSharedSkipElements() {
        val filterContext = FilterContext(profile = FilterProfile.StandardImportFilter, codespace = "TST")
        val config = StandardImportFilteringProfileConfig().build(filterContext)

        assertTrue(
            config.skipElements.containsAll(
                listOf(
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
        val filterContext = FilterContext(profile = FilterProfile.StandardImportFilter, codespace = "TST")
        val config = StandardImportFilteringProfileConfig().build(filterContext)

        assertFalse(config.preserveComments)
        assertTrue(config.pruneReferences)
        assertTrue(config.referencesToExcludeFromPruning.contains("QuayRef"))
        assertTrue(config.useSelfClosingTagsWhereApplicable)
    }

    @Test
    fun testSharedUnreferencedEntitiesToPrune() {
        val filterContext = FilterContext(profile = FilterProfile.StandardImportFilter, codespace = "TST")
        val config = StandardImportFilteringProfileConfig().build(filterContext)

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
                    "DayType",
                    "OperatingPeriod",
                    "OperatingDay"
                )
            )
        )
    }

    @Test
    fun testSharedRequiredChildren() {
        val filterContext = FilterContext(profile = FilterProfile.StandardImportFilter, codespace = "TST")
        val config = StandardImportFilteringProfileConfig().build(filterContext)

        assertTrue(config.elementsRequiredChildren.containsKey("NoticeAssignment"))
        assertTrue(config.elementsRequiredChildren["NoticeAssignment"]!!.containsAll(listOf("NoticeRef", "NoticedObjectRef")))
    }
}
