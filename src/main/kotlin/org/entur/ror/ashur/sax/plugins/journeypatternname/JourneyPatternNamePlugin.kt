package org.entur.ror.ashur.sax.plugins.journeypatternname

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.plugin.AbstractNetexPlugin
import org.entur.ror.ashur.sax.NetexElementNames.NAME
import org.entur.ror.ashur.sax.NetexElementNames.ROUTE_REF
import org.entur.ror.ashur.sax.plugins.journeypatternname.handlers.NameHandler
import org.entur.ror.ashur.sax.plugins.journeypatternname.handlers.RouteRefHandler
import org.xml.sax.Attributes

class JourneyPatternNamePlugin(
    val repository: JourneyPatternNameRepository
) : AbstractNetexPlugin() {

    private val elementHandlers: Map<String, JourneyPatternNameDataCollector> by lazy {
        mapOf(
            NAME to NameHandler(repository),
            ROUTE_REF to RouteRefHandler(repository),
        )
    }

    override fun getName(): String = "JourneyPatternNamePlugin"

    override fun getDescription(): String =
        "Collects Route names and JourneyPattern RouteRef mappings to enable Name propagation"

    override fun getSupportedElementTypes(): Set<String> = elementHandlers.keys.toSet()

    override fun startElement(elementName: String, attributes: Attributes?, currentEntity: Entity?) {
        currentEntity?.let { entity ->
            elementHandlers[elementName]?.startElement(attributes, entity)
        }
    }

    override fun characters(elementName: String, ch: CharArray?, start: Int, length: Int) {
        elementHandlers[elementName]?.characters(ch, start, length)
    }

    override fun endElement(elementName: String, currentEntity: Entity?) {
        currentEntity?.let { entity ->
            elementHandlers[elementName]?.endElement(entity)
        }
    }

    override fun getCollectedData(): JourneyPatternNameRepository = repository
}
