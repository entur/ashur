package org.entur.ror.ashur.filter

import org.entur.netex.tools.lib.config.FilterConfig
import org.entur.netex.tools.lib.config.FilterConfigBuilder
import org.entur.netex.tools.lib.config.TimePeriod
import java.time.LocalDate

class StandardImportFilteringProfileConfig: FilterProfileConfiguration {
    override fun build(): FilterConfig =
        FilterConfigBuilder()
            .withPeriod(TimePeriod(
                start = LocalDate.now().minusDays(2),
                end = LocalDate.now().plusYears(1)
            ))
            .withSkipElements(
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
            )
            .withRemovePrivateData(true)
            .withPreserveComments(false)
            .withUseSelfClosingTagsWhereApplicable(true)
            .withPruneReferences(true)
            .withReferencesToExcludeFromPruning(setOf("QuayRef"))
            .withUnreferencedEntitiesToPrune(
                setOf(
                    "JourneyPattern",
                    "Route",
                    "Network",
                    "Line",
                    "Operator",
                    "Notice",
                    "DestinationDisplay",
                    "ServiceLink",
                )
            )
            .build()
}