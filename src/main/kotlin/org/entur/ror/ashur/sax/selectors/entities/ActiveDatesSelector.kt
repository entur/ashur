package org.entur.ror.ashur.sax.selectors.entities

import org.entur.netex.tools.lib.config.TimePeriod
import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.selections.EntitySelection
import org.entur.netex.tools.lib.selectors.entities.EntitySelector
import org.entur.netex.tools.lib.selectors.entities.EntitySelectorContext
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesCalculator
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesRepository
import kotlin.collections.forEach

class ActiveDatesSelector(val activeDatesRepository: ActiveDatesRepository, val period: TimePeriod): EntitySelector {
    override fun selectEntities(context: EntitySelectorContext): EntitySelection {
        val calculator = ActiveDatesCalculator(activeDatesRepository)
        val model = context.entityModel
        val activeEntities = calculator.activeDateEntitiesInPeriod(period, model)
        val activeEntitiesMap = mutableMapOf<String, MutableMap<String, Entity>>()

        val entitiesByTypeAndId = model.getEntitesByTypeAndId()
        entitiesByTypeAndId.forEach { (type, entities) ->
            if (activeEntities.containsKey(type)) {
                val idsOfActiveEntitiesWithType = activeEntities[type]
                val entitiesToKeep = entities.filter { idsOfActiveEntitiesWithType?.contains(it.key) == true  }
                if (entitiesToKeep.isNotEmpty()) {
                    activeEntitiesMap[type] = entitiesToKeep.toMutableMap()
                }
            } else {
                // If no active entities for this type, keep all entities of this type
                activeEntitiesMap[type] = entities.toMutableMap()
            }
        }
        return EntitySelection(activeEntitiesMap, model)
    }
}