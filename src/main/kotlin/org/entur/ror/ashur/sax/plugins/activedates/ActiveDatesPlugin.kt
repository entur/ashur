package org.entur.ror.ashur.sax.plugins.activedates

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.netex.tools.lib.plugin.AbstractNetexPlugin
import org.entur.ror.ashur.sax.plugins.activedates.handlers.ArrivalDayOffsetHandler
import org.entur.ror.ashur.sax.plugins.activedates.handlers.ArrivalTimeHandler
import org.entur.ror.ashur.sax.plugins.activedates.handlers.CalendarDateHandler
import org.entur.ror.ashur.sax.plugins.activedates.handlers.DateHandler
import org.entur.ror.ashur.sax.plugins.activedates.handlers.DatedServiceJourneyHandler
import org.entur.ror.ashur.sax.plugins.activedates.handlers.DayTypeAssignmentHandler
import org.entur.ror.ashur.sax.plugins.activedates.handlers.DayTypeRefHandler
import org.entur.ror.ashur.sax.plugins.activedates.handlers.DaysOfWeekHandler
import org.entur.ror.ashur.sax.plugins.activedates.handlers.DeadRunHandler
import org.entur.ror.ashur.sax.plugins.activedates.handlers.FromDateHandler
import org.entur.ror.ashur.sax.plugins.activedates.handlers.FromDateRefHandler
import org.entur.ror.ashur.sax.plugins.activedates.handlers.OperatingDayRefHandler
import org.entur.ror.ashur.sax.plugins.activedates.handlers.OperatingPeriodRefHandler
import org.entur.ror.ashur.sax.plugins.activedates.handlers.ServiceJourneyHandler
import org.entur.ror.ashur.sax.plugins.activedates.handlers.ServiceJourneyRefHandler
import org.entur.ror.ashur.sax.plugins.activedates.handlers.ToDateHandler
import org.entur.ror.ashur.sax.plugins.activedates.handlers.ToDateRefHandler
import org.xml.sax.Attributes

/**
 * Plugin that collects date-related data from NeTEx files to enable date-based filtering.
 * This plugin maintains all the functionality of the original ActiveDatesModel system
 * but works within the new plugin architecture.
 */
class ActiveDatesPlugin (
    val activeDatesRepository : ActiveDatesRepository
) : AbstractNetexPlugin() {

    private val parsingContext : ActiveDatesParsingContext = ActiveDatesParsingContext()
    
    // Map of element handlers - delegating to existing collector implementations
    private val elementHandlers: Map<String, ActiveDatesDataCollector> by lazy {
        mapOf(
            NetexTypes.CALENDAR_DATE to CalendarDateHandler(activeDatesRepository),
            NetexTypes.DAY_TYPE_ASSIGNMENT to DayTypeAssignmentHandler(activeDatesRepository),
            NetexTypes.OPERATING_DAY_REF to OperatingDayRefHandler(activeDatesRepository),
            NetexTypes.OPERATING_PERIOD_REF to OperatingPeriodRefHandler(activeDatesRepository),
            NetexTypes.DAY_TYPE_REF to DayTypeRefHandler(activeDatesRepository),
            NetexTypes.FROM_DATE to FromDateHandler(activeDatesRepository),
            NetexTypes.TO_DATE to ToDateHandler(activeDatesRepository),
            NetexTypes.FROM_DATE_REF to FromDateRefHandler(activeDatesRepository),
            NetexTypes.TO_DATE_REF to ToDateRefHandler(activeDatesRepository),
            NetexTypes.DATE to DateHandler(activeDatesRepository),
            NetexTypes.DAYS_OF_WEEK to DaysOfWeekHandler(activeDatesRepository),
            NetexTypes.ARRIVAL_TIME to ArrivalTimeHandler(activeDatesRepository),
            NetexTypes.ARRIVAL_DAY_OFFSET to ArrivalDayOffsetHandler(activeDatesRepository),
            NetexTypes.SERVICE_JOURNEY to ServiceJourneyHandler(activeDatesRepository),
            NetexTypes.SERVICE_JOURNEY_REF to ServiceJourneyRefHandler(activeDatesRepository),
            NetexTypes.DATED_SERVICE_JOURNEY to DatedServiceJourneyHandler(activeDatesRepository),
            NetexTypes.DEAD_RUN to DeadRunHandler(activeDatesRepository),
        )
    }
    
    override fun getName(): String = "ActiveDatesPlugin"
    
    override fun getDescription(): String = 
        "Collects date-related data from NeTEx elements to enable date-based filtering of service journeys and related entities"
    
    override fun getSupportedElementTypes(): Set<String> = elementHandlers.keys.toSet()
    
    override fun startElement(elementName: String, attributes: Attributes?, currentEntity: Entity?) {
        currentEntity?.let { entity ->
            elementHandlers[elementName]?.startElement(parsingContext, attributes, entity)
        }
    }
    
    override fun characters(elementName: String, ch: CharArray?, start: Int, length: Int) {
        elementHandlers[elementName]?.characters(parsingContext, ch, start, length)
    }
    
    override fun endElement(elementName: String, currentEntity: Entity?) {
        currentEntity?.let { entity ->
            elementHandlers[elementName]?.endElement(parsingContext, entity)
        }
    }
    
    override fun getCollectedData(): ActiveDatesRepository {
        return activeDatesRepository
    }
}