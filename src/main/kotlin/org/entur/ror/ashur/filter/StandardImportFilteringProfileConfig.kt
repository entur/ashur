package org.entur.ror.ashur.filter

import org.entur.netex.tools.lib.config.FilterConfig
import org.entur.netex.tools.lib.config.FilterConfigBuilder
import org.entur.netex.tools.lib.config.TimePeriod
import org.entur.netex.tools.lib.output.SkipElementHandler
import org.entur.netex.tools.lib.output.XMLElementHandler
import org.entur.ror.ashur.sax.handlers.DefaultLocaleHandler
import org.entur.ror.ashur.sax.handlers.QuayRefHandler
import org.entur.ror.ashur.sax.handlers.ValidBetweenFromDateHandler
import org.entur.ror.ashur.sax.handlers.ValidBetweenHandler
import org.entur.ror.ashur.sax.handlers.ValidBetweenToDateHandler
import java.time.LocalDate

class StandardImportFilteringProfileConfig: FilterProfileConfiguration {
    private fun customElementHandlers(period: TimePeriod, codespace: String): Map<String, XMLElementHandler> {
        val skipElementHandler = SkipElementHandler()
        val validBetweenHandler = ValidBetweenHandler(codespace)
        val quayRefHandler = QuayRefHandler()
        val validBetweenFromDateHandler = ValidBetweenFromDateHandler(fromDate = period.start!!)
        val validBetweenToDateHandler = ValidBetweenToDateHandler(toDate = period.end!!)
        val defaultLocaleHandler = DefaultLocaleHandler()
        return mapOf(
            "/PublicationDelivery/dataObjects/ServiceCalendarFrame/ServiceCalendar" to skipElementHandler,
            "/PublicationDelivery/dataObjects/CompositeFrame/frames/ServiceCalendarFrame/ServiceCalendar" to skipElementHandler,
            "/PublicationDelivery/dataObjects/CompositeFrame/frames/ServiceFrame/stopAssignments/PassengerStopAssignment/QuayRef" to quayRefHandler,
            "/PublicationDelivery/dataObjects/CompositeFrame/validityConditions/ValidBetween" to validBetweenHandler,
            "/PublicationDelivery/dataObjects/CompositeFrame/validityConditions/ValidBetween/FromDate" to validBetweenFromDateHandler,
            "/PublicationDelivery/dataObjects/CompositeFrame/validityConditions/ValidBetween/ToDate" to validBetweenToDateHandler,
            "/PublicationDelivery/dataObjects/CompositeFrame/FrameDefaults/DefaultLocale/TimeZone" to skipElementHandler,
            "/PublicationDelivery/dataObjects/CompositeFrame/FrameDefaults/DefaultLocale/DefaultLanguage" to skipElementHandler,
            "/PublicationDelivery/dataObjects/CompositeFrame/FrameDefaults/DefaultLocale" to defaultLocaleHandler,
        )
    }

    private fun skipElements(): List<String> = listOf(
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
        "/PublicationDelivery/dataObjects/CompositeFrame/frames/SiteFrame",
        "/PublicationDelivery/dataObjects/SiteFrame"
    )

    override fun build(codespace: String): FilterConfig {
        val timePeriod = TimePeriod(
            start = LocalDate.now().minusDays(2),
            end = LocalDate.now().plusYears(1)
        )
        return FilterConfigBuilder()
            .withCustomElementHandlers(customElementHandlers(timePeriod, codespace))
            .withPeriod(timePeriod)
            .withSkipElements(skipElements())
            .withRenameFiles(true)
            .withRemoveInterchangesWithoutServiceJourneys(true)
            .withRemovePassengerStopAssignmentsWithUnreferredScheduledStopPoint(true)
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
}