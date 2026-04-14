package org.entur.ror.ashur.sax.plugins.journeypatternname

import org.entur.netex.tools.lib.model.Entity
import org.xml.sax.Attributes

abstract class JourneyPatternNameDataCollector {
    open fun startElement(attributes: Attributes?, currentEntity: Entity) {}
    open fun characters(ch: CharArray?, start: Int, length: Int) {}
    open fun endElement(currentEntity: Entity) {}
}
