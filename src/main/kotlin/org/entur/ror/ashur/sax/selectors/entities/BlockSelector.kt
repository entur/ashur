package org.entur.ror.ashur.sax.selectors.entities

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.selections.EntitySelection
import org.entur.netex.tools.lib.selectors.entities.EntitySelector
import org.entur.netex.tools.lib.selectors.entities.EntitySelectorContext

class BlockSelector: EntitySelector {
    override fun selectEntities(context: EntitySelectorContext): EntitySelection {
        val model = context.entityModel
        val entitySelection = context.currentEntitySelection!!

        val blockEntities = model.getEntitiesOfType("Block").toSet()
        val blocksToKeep = mutableMapOf<String, Entity>()

        for (block in blockEntities) {
            val journeyRefs = model.getRefsOfTypeFrom(block.id, "VehicleJourneyRef")
            val dayTypeRefs = model.getRefsOfTypeFrom(block.id, "DayTypeRef")

            val blockRefersToExistingJourney = journeyRefs.any { entitySelection.isSelected("ServiceJourney", it.ref) }
            val blockRefersToExistingDayType = dayTypeRefs.any { entitySelection.isSelected("DayType", it.ref) }

            if (!blockRefersToExistingJourney || !blockRefersToExistingDayType) {
                continue
            }

            blocksToKeep[block.id] = block
        }

        return entitySelection.withReplaced("Block", blocksToKeep)
    }
}
