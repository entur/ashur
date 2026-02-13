package org.entur.ror.ashur.filter

import org.entur.netex.tools.lib.config.FilterConfig
import org.entur.netex.tools.lib.config.FilterConfigBuilder
import org.entur.netex.tools.lib.config.TimePeriod
import org.entur.netex.tools.lib.output.SkipElementHandler
import org.entur.netex.tools.lib.output.XMLElementHandler
import org.entur.ror.ashur.sax.handlers.CodespacesHandler
import org.entur.ror.ashur.sax.handlers.CompositeFrameHandler
import org.entur.ror.ashur.sax.handlers.QuayRefHandler
import org.entur.ror.ashur.sax.handlers.ValidBetweenFromDateHandler
import org.entur.ror.ashur.sax.handlers.ValidBetweenHandler
import org.entur.ror.ashur.sax.handlers.ValidBetweenToDateHandler
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesPlugin
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesRepository
import org.entur.ror.ashur.sax.plugins.filenames.FileNamePlugin
import org.entur.ror.ashur.sax.plugins.filenames.FileNameRepository
import org.entur.ror.ashur.sax.selectors.entities.ActiveDatesSelector
import org.entur.ror.ashur.sax.selectors.entities.PassengerStopAssignmentSelector
import org.entur.ror.ashur.sax.selectors.entities.ServiceJourneyInterchangeSelector
import org.entur.ror.ashur.sax.selectors.refs.ActiveDatesRefSelector
import java.time.LocalDate
import java.time.LocalDateTime

class StandardImportFilteringProfileConfig: FilterProfileConfiguration {
    private fun customElementHandlers(
        period: TimePeriod,
        codespace: String,
        fileCreatedAt: LocalDateTime?
    ): Map<String, XMLElementHandler> {
        val compositeFrameHandler = CompositeFrameHandler(fileCreatedAt)
        val skipElementHandler = SkipElementHandler()
        val validBetweenHandler = ValidBetweenHandler(codespace)
        val quayRefHandler = QuayRefHandler()
        val codespacesHandler = CodespacesHandler()
        val validBetweenFromDateHandler = ValidBetweenFromDateHandler(fromDate = period.start!!)
        val validBetweenToDateHandler = ValidBetweenToDateHandler(toDate = period.end!!)
        return mapOf(
            "/PublicationDelivery/dataObjects/CompositeFrame" to compositeFrameHandler,
            "/PublicationDelivery/dataObjects/CompositeFrame/codespaces" to codespacesHandler,
            "/PublicationDelivery/dataObjects/CompositeFrame/FrameDefaults" to skipElementHandler,
            "/PublicationDelivery/dataObjects/ServiceCalendarFrame/ServiceCalendar" to skipElementHandler,
            "/PublicationDelivery/dataObjects/CompositeFrame/frames/ServiceCalendarFrame/ServiceCalendar" to skipElementHandler,
            "/PublicationDelivery/dataObjects/CompositeFrame/frames/ServiceCalendarFrame/ServiceCalendar/FromDate" to skipElementHandler,
            "/PublicationDelivery/dataObjects/CompositeFrame/frames/ServiceCalendarFrame/ServiceCalendar/ToDate" to skipElementHandler,
            "/PublicationDelivery/dataObjects/CompositeFrame/frames/ServiceFrame/stopAssignments/PassengerStopAssignment/QuayRef" to quayRefHandler,
            "/PublicationDelivery/dataObjects/CompositeFrame/validityConditions/ValidBetween" to validBetweenHandler,
            "/PublicationDelivery/dataObjects/CompositeFrame/validityConditions/ValidBetween/FromDate" to validBetweenFromDateHandler,
            "/PublicationDelivery/dataObjects/CompositeFrame/validityConditions/ValidBetween/ToDate" to validBetweenToDateHandler,
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

    override fun build(filterContext: FilterContext): FilterConfig {
        val timePeriod = TimePeriod(
            start = LocalDate.now().minusDays(2),
            end = LocalDate.now().plusYears(1)
        )
        val codespace = filterContext.codespace
        val fileCreatedAt = filterContext.fileCreatedAt
        val activeDatesRepository = ActiveDatesRepository()
        val fileNameRepository = FileNameRepository()
        return FilterConfigBuilder()
            .withCustomElementHandlers(customElementHandlers(timePeriod, codespace, fileCreatedAt))
            .withSkipElements(skipElements())
            .withRemovePrivateData(true)
            .withPreserveComments(false)
            .withUseSelfClosingTagsWhereApplicable(true)
            .withPruneReferences(true)
            .withReferencesToExcludeFromPruning(setOf("QuayRef"))
            .withUnreferencedEntitiesToPrune(
                setOf(
                    "JourneyPattern",
                    "Route",
                    "Line",
                    "Operator",
                    "Notice",
                    "DestinationDisplay",
                    "ServiceLink",
                    "DayType",
                    "OperatingPeriod",
                    "OperatingDay",
                )
            )
            .withElementsRequiredChildren(
                mapOf(
                    "NoticeAssignment" to listOf("NoticeRef", "NoticedObjectRef")
                )
            )
            .withPlugins(
                listOf(
                    ActiveDatesPlugin(activeDatesRepository),
                    FileNamePlugin(
                        fileNameRepository = fileNameRepository,
                        codespace = codespace
                    )
                )
            )
            .withFileNameMap(fileNameRepository.filesToRename)
            .withEntitySelectors(
                listOf(
                    ActiveDatesSelector(
                        activeDatesRepository = activeDatesRepository,
                        period = timePeriod
                    ),
                    PassengerStopAssignmentSelector(),
                    ServiceJourneyInterchangeSelector()
                )
            )
            .withRefSelectors(
                listOf(
                    ActiveDatesRefSelector(
                        activeDatesRepository = activeDatesRepository,
                        period = timePeriod
                    ),
                )
            )
            .build()
    }
}