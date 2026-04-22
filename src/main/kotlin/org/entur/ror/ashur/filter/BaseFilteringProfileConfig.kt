package org.entur.ror.ashur.filter

import org.entur.netex.tools.lib.config.FilterConfig
import org.entur.netex.tools.lib.config.FilterConfigBuilder
import org.entur.netex.tools.lib.config.TimePeriod
import org.entur.netex.tools.lib.output.SkipElementHandler
import org.entur.netex.tools.lib.output.XMLElementHandler
import org.entur.netex.tools.lib.selectors.entities.EntitySelector
import org.entur.ror.ashur.sax.handlers.CodespacesHandler
import org.entur.ror.ashur.sax.handlers.CompositeFrameHandler
import org.entur.ror.ashur.sax.handlers.JourneyPatternWithNameHandler
import org.entur.ror.ashur.sax.handlers.PublicationDeliveryHandler
import org.entur.ror.ashur.sax.handlers.QuayRefHandler
import org.entur.ror.ashur.sax.handlers.ServiceJourneyHandler
import org.entur.ror.ashur.sax.handlers.ServiceJourneyLineRefHandler
import org.entur.ror.ashur.sax.handlers.ServiceJourneyOperatorRefContext
import org.entur.ror.ashur.sax.handlers.ValidBetweenFromDateHandler
import org.entur.ror.ashur.sax.handlers.ValidBetweenHandler
import org.entur.ror.ashur.sax.handlers.ValidBetweenToDateHandler
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesPlugin
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesRepository
import org.entur.ror.ashur.sax.plugins.filenames.FileNamePlugin
import org.entur.ror.ashur.sax.plugins.filenames.FileNameRepository
import org.entur.ror.ashur.sax.plugins.journeypatternname.JourneyPatternNamePlugin
import org.entur.ror.ashur.sax.plugins.journeypatternname.JourneyPatternNameRepository
import org.entur.ror.ashur.sax.plugins.operatorref.OperatorRefPlugin
import org.entur.ror.ashur.sax.plugins.operatorref.OperatorRefRepository
import org.entur.ror.ashur.sax.selectors.entities.ActiveDatesSelector
import org.entur.ror.ashur.sax.selectors.entities.PassengerStopAssignmentSelector
import org.entur.ror.ashur.sax.selectors.entities.ServiceJourneyInterchangeSelector
import org.entur.ror.ashur.sax.selectors.refs.ActiveDatesRefSelector
import java.time.LocalDate
import java.time.LocalDateTime

abstract class BaseFilteringProfileConfig : FilterProfileConfiguration {

    protected abstract val removePrivateData: Boolean

    protected open fun includeElements(): List<String> = emptyList()

    protected open fun additionalEntitySelectors(
        activeDatesRepository: ActiveDatesRepository,
        timePeriod: TimePeriod
    ): List<EntitySelector> = emptyList()

    protected open fun additionalElementsRequiredChildren(): Map<String, List<String>> = emptyMap()

    companion object {
        fun standardTimePeriod(): TimePeriod = TimePeriod(
            start = LocalDate.now().minusDays(2),
            end = LocalDate.now().plusYears(1)
        )
    }

    private fun customElementHandlers(
        period: TimePeriod,
        codespace: String,
        fileCreatedAt: LocalDateTime?,
        journeyPatternNameRepository: JourneyPatternNameRepository,
        operatorRefRepository: OperatorRefRepository
    ): Map<String, XMLElementHandler> {
        val publicationDeliveryHandler = PublicationDeliveryHandler()
        val compositeFrameHandler = CompositeFrameHandler(fileCreatedAt)
        val skipElementHandler = SkipElementHandler()
        val validBetweenHandler = ValidBetweenHandler(codespace)
        val quayRefHandler = QuayRefHandler()
        val codespacesHandler = CodespacesHandler()
        val validBetweenFromDateHandler = ValidBetweenFromDateHandler(fromDate = period.start!!)
        val validBetweenToDateHandler = ValidBetweenToDateHandler(toDate = period.end!!)
        val journeyPatternWithNameHandler = JourneyPatternWithNameHandler(journeyPatternNameRepository)
        val operatorRefContext = ServiceJourneyOperatorRefContext()
        val serviceJourneyHandler = ServiceJourneyHandler(operatorRefContext)
        val serviceJourneyLineRefHandler = ServiceJourneyLineRefHandler(operatorRefContext, operatorRefRepository)
        return mapOf(
            "/PublicationDelivery" to publicationDeliveryHandler,
            "/PublicationDelivery/dataObjects/CompositeFrame" to compositeFrameHandler,
            "/PublicationDelivery/dataObjects/CompositeFrame/codespaces" to codespacesHandler,
            "/PublicationDelivery/dataObjects/ServiceCalendarFrame/ServiceCalendar" to skipElementHandler,
            "/PublicationDelivery/dataObjects/CompositeFrame/frames/ServiceCalendarFrame/ServiceCalendar" to skipElementHandler,
            "/PublicationDelivery/dataObjects/CompositeFrame/frames/ServiceCalendarFrame/ServiceCalendar/FromDate" to skipElementHandler,
            "/PublicationDelivery/dataObjects/CompositeFrame/frames/ServiceCalendarFrame/ServiceCalendar/ToDate" to skipElementHandler,
            "/PublicationDelivery/dataObjects/CompositeFrame/frames/ServiceFrame/stopAssignments/PassengerStopAssignment/QuayRef" to quayRefHandler,
            "/PublicationDelivery/dataObjects/CompositeFrame/validityConditions/ValidBetween" to validBetweenHandler,
            "/PublicationDelivery/dataObjects/CompositeFrame/validityConditions/ValidBetween/FromDate" to validBetweenFromDateHandler,
            "/PublicationDelivery/dataObjects/CompositeFrame/validityConditions/ValidBetween/ToDate" to validBetweenToDateHandler,
            "/PublicationDelivery/dataObjects/CompositeFrame/frames/ServiceFrame/journeyPatterns/JourneyPattern" to journeyPatternWithNameHandler,
            "/PublicationDelivery/dataObjects/ServiceFrame/journeyPatterns/JourneyPattern" to journeyPatternWithNameHandler,
            "/PublicationDelivery/dataObjects/CompositeFrame/frames/TimetableFrame/vehicleJourneys/ServiceJourney" to serviceJourneyHandler,
            "/PublicationDelivery/dataObjects/TimetableFrame/vehicleJourneys/ServiceJourney" to serviceJourneyHandler,
            "/PublicationDelivery/dataObjects/CompositeFrame/frames/TimetableFrame/vehicleJourneys/ServiceJourney/LineRef" to serviceJourneyLineRefHandler,
            "/PublicationDelivery/dataObjects/TimetableFrame/vehicleJourneys/ServiceJourney/LineRef" to serviceJourneyLineRefHandler,
        )
    }

    private fun skipElements(): List<String> = listOf(
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
        "/PublicationDelivery/dataObjects/SiteFrame",
        "/PublicationDelivery/dataObjects/CompositeFrame/frames/VehicleScheduleFrame",
        "/PublicationDelivery/dataObjects/VehicleScheduleFrame"
    )

    override fun build(filterContext: FilterContext): FilterConfig {
        val timePeriod = standardTimePeriod()
        val codespace = filterContext.codespace
        val fileCreatedAt = filterContext.fileCreatedAt
        val activeDatesRepository = ActiveDatesRepository()
        val fileNameRepository = FileNameRepository()
        val journeyPatternNameRepository = JourneyPatternNameRepository()
        val operatorRefRepository = OperatorRefRepository()
        return FilterConfigBuilder()
            .withCustomElementHandlers(customElementHandlers(timePeriod, codespace, fileCreatedAt, journeyPatternNameRepository, operatorRefRepository))
            .withSkipElements(skipElements().filterNot { it in includeElements() })
            .withRemovePrivateData(removePrivateData)
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
                    "ScheduledStopPoint",
                    "RoutePoint",
                    "DayType",
                    "OperatingPeriod",
                    "OperatingDay",
                )
            )
            .withElementsRequiredChildren(
                mapOf(
                    "NoticeAssignment" to listOf("NoticeRef", "NoticedObjectRef")
                ) + additionalElementsRequiredChildren()
            )
            .withPlugins(
                listOf(
                    ActiveDatesPlugin(activeDatesRepository),
                    FileNamePlugin(
                        fileNameRepository = fileNameRepository,
                        codespace = codespace
                    ),
                    JourneyPatternNamePlugin(journeyPatternNameRepository),
                    OperatorRefPlugin(operatorRefRepository)
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
                ) + additionalEntitySelectors(activeDatesRepository, timePeriod)
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
