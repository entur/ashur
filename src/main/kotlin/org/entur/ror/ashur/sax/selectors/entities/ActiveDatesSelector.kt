package org.entur.ror.ashur.sax.selectors.entities

import org.entur.netex.tools.lib.config.TimePeriod
import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.selections.EntitySelection
import org.entur.netex.tools.lib.selectors.entities.EntitySelector
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesCalculator
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesRepository
import kotlin.collections.forEach

class ActiveDatesSelector(val activeDatesRepository: ActiveDatesRepository, val period: TimePeriod): EntitySelector {
    override fun selectEntities(model: EntityModel, currentEntitySelection: EntitySelection?): EntitySelection {
        val calculator = ActiveDatesCalculator(activeDatesRepository)
        val activeEntities = calculator.activeDateEntitiesInPeriod(period, model)
        val activeEntitiesMap = mutableMapOf<String, MutableMap<String, Entity>>()

        val entitiesByTypeAndId = model.getEntitesByTypeAndId()
        entitiesByTypeAndId.forEach { (type, entities) ->
            if (activeEntities.containsKey(type)) {
                val idsOfActiveEntitiesWithType = activeEntities[type]
                val entitiesToKeep = entities.filter { idsOfActiveEntitiesWithType?.contains(it.key) == true  }
                if (entitiesToKeep.isNotEmpty()) {
                    activeEntitiesMap.put(type, entitiesToKeep.toMutableMap())
                }
            } else {
                // If no active entities for this type, keep all entities of this type
                activeEntitiesMap.put(type, entities.toMutableMap())
            }
        }
        return EntitySelection(activeEntitiesMap, model)
    }
}