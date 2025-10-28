package org.entur.ror.ashur.filter

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate

class StandardImportFilteringProfileConfigTest {
    @Test
    fun testStandardImportFilteringProfileConfig() {
        val config = StandardImportFilteringProfileConfig().build()
        Assertions.assertTrue(config.period.start!!.isEqual(LocalDate.now().minusDays(2)))
        Assertions.assertTrue(config.period.end!!.isEqual(LocalDate.now().plusYears(1)))
        Assertions.assertTrue(config.skipElements.containsAll(
            listOf(
                "/PublicationDelivery/dataObjects/CompositeFrame/frames/VehicleScheduleFrame",
                "/PublicationDelivery/dataObjects/VehicleScheduleFrame",
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
            )
        ))
        Assertions.assertTrue(config.removePrivateData)
        Assertions.assertFalse(config.preserveComments)
        Assertions.assertTrue(config.pruneReferences)
        Assertions.assertTrue(config.referencesToExcludeFromPruning.contains("QuayRef"))
        Assertions.assertTrue(config.unreferencedEntitiesToPrune.containsAll(
            listOf(
                "JourneyPattern",
                "Route",
                "Network",
                "Line",
                "Operator",
                "Notice",
                "DestinationDisplay",
                "ServiceLink",
            ))
        )
        Assertions.assertTrue(config.useSelfClosingTagsWhereApplicable)
    }
}