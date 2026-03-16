package org.entur.ror.ashur.sax.plugins.servicejourneynaming

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.netex.tools.lib.plugin.AbstractNetexPlugin
import org.entur.ror.ashur.sax.plugins.servicejourneynaming.handlers.DestinationDisplayHandler
import org.entur.ror.ashur.sax.plugins.servicejourneynaming.handlers.DestinationDisplayRefHandler
import org.entur.ror.ashur.sax.plugins.servicejourneynaming.handlers.FrontTextHandler
import org.entur.ror.ashur.sax.plugins.servicejourneynaming.handlers.JourneyPatternHandler
import org.entur.ror.ashur.sax.plugins.servicejourneynaming.handlers.JourneyPatternRefHandler
import org.entur.ror.ashur.sax.plugins.servicejourneynaming.handlers.ServiceJourneyNameElementHandler
import org.entur.ror.ashur.sax.plugins.servicejourneynaming.handlers.ServiceJourneyPluginHandler
import org.entur.ror.ashur.sax.plugins.servicejourneynaming.handlers.StopPointInJourneyPatternHandler
import org.xml.sax.Attributes

/**
 * Plugin that collects data needed to inject Name elements into ServiceJourney.
 *
 * Collects:
 * - DestinationDisplay ID -> FrontText value
 * - JourneyPattern ID -> DestinationDisplay ID (from first stop)
 * - ServiceJourney ID -> JourneyPattern ID
 * - ServiceJourney IDs that already have Name elements
 *
 * The data chain is:
 * ServiceJourney -> JourneyPatternRef -> JourneyPattern -> StopPointInJourneyPattern (order=1)
 *                -> DestinationDisplayRef -> DestinationDisplay -> FrontText
 */
class ServiceJourneyNamePlugin(
    val repository: ServiceJourneyNameRepository
) : AbstractNetexPlugin() {

    private val parsingContext = ServiceJourneyNameParsingContext()

    private val elementHandlers: Map<String, ServiceJourneyNameDataCollector> by lazy {
        mapOf(
            NetexTypes.DESTINATION_DISPLAY to DestinationDisplayHandler(),
            FRONT_TEXT to FrontTextHandler(repository),
            NetexTypes.JOURNEY_PATTERN to JourneyPatternHandler(),
            NetexTypes.STOP_POINT_IN_JOURNEY_PATTERN to StopPointInJourneyPatternHandler(),
            DESTINATION_DISPLAY_REF to DestinationDisplayRefHandler(repository),
            NetexTypes.SERVICE_JOURNEY to ServiceJourneyPluginHandler(),
            JOURNEY_PATTERN_REF to JourneyPatternRefHandler(repository),
            NetexTypes.NAME to ServiceJourneyNameElementHandler(repository),
        )
    }

    override fun getName(): String = "ServiceJourneyNamePlugin"

    override fun getDescription(): String =
        "Collects data to inject Name element into ServiceJourney from DestinationDisplay FrontText"

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

    override fun getCollectedData(): ServiceJourneyNameRepository = repository

    companion object {
        // Element names not in NetexTypes
        private const val FRONT_TEXT = "FrontText"
        private const val DESTINATION_DISPLAY_REF = "DestinationDisplayRef"
        private const val JOURNEY_PATTERN_REF = "JourneyPatternRef"
    }
}
