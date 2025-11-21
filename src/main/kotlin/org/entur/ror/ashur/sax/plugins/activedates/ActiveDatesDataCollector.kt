package org.entur.ror.ashur.sax.plugins.activedates

import org.entur.netex.tools.lib.model.Entity
import org.xml.sax.Attributes

abstract class ActiveDatesDataCollector {
    open fun characters(context: ActiveDatesParsingContext, ch: CharArray?, start: Int, length: Int) {}
    open fun endElement(context: ActiveDatesParsingContext, currentEntity: Entity) {}
    open fun startElement(context: ActiveDatesParsingContext, attributes: Attributes?, currentEntity: Entity) {}
}
