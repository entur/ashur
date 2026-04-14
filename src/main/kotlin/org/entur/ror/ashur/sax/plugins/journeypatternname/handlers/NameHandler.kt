package org.entur.ror.ashur.sax.plugins.journeypatternname.handlers

import org.entur.netex.tools.lib.model.Entity
import org.entur.ror.ashur.sax.NetexElementNames.JOURNEY_PATTERN
import org.entur.ror.ashur.sax.NetexElementNames.ROUTE
import org.entur.ror.ashur.sax.plugins.journeypatternname.JourneyPatternNameDataCollector
import org.entur.ror.ashur.sax.plugins.journeypatternname.JourneyPatternNameRepository
import org.xml.sax.Attributes

class NameHandler(
    private val repository: JourneyPatternNameRepository
) : JourneyPatternNameDataCollector() {

    private val stringBuilder = StringBuilder()

    override fun startElement(attributes: Attributes?, currentEntity: Entity) {
        stringBuilder.clear()
        if (currentEntity.type == JOURNEY_PATTERN) {
            repository.journeyPatternsWithName.add(currentEntity.id)
        }
    }

    override fun characters(ch: CharArray?, start: Int, length: Int) {
        ch?.let { stringBuilder.append(it, start, length) }
    }

    override fun endElement(currentEntity: Entity) {
        if (currentEntity.type == ROUTE) {
            val name = stringBuilder.toString().trim()
            if (name.isNotEmpty()) {
                repository.routeNames[currentEntity.id] = name
            }
        }
        stringBuilder.clear()
    }
}
