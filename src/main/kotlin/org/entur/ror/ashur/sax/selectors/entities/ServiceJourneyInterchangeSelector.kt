package org.entur.ror.ashur.sax.selectors.entities

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.selections.EntitySelection
import org.entur.netex.tools.lib.selectors.entities.EntitySelector

class ServiceJourneyInterchangeSelector: EntitySelector {
    override fun selectEntities(model: EntityModel, currentEntitySelection: EntitySelection?): EntitySelection {
        val entitySelection = currentEntitySelection!!
        val serviceJourneyInterchangeEntities = model.getEntitiesOfType("ServiceJourneyInterchange").toSet()

        val serviceJourneyInterchangesToKeep = serviceJourneyInterchangeEntities
            .filter { serviceJourneyInterchange ->
                val fromJourneyRef = model.getRefsOfTypeFrom(serviceJourneyInterchange.id, "FromJourneyRef").firstOrNull()?.ref
                val toJourneyRef = model.getRefsOfTypeFrom(serviceJourneyInterchange.id, "ToJourneyRef").firstOrNull()?.ref
                val hasRefToFromJourney = entitySelection.isSelected("ServiceJourney", fromJourneyRef)
                val hasRefToToJourney = entitySelection.isSelected("ServiceJourney", toJourneyRef)
                hasRefToFromJourney && hasRefToToJourney
            }

        val serviceJourneyInterchangeMap = mutableMapOf<String, Entity>()
        serviceJourneyInterchangesToKeep.forEach { interchange ->
            serviceJourneyInterchangeMap[interchange.id] = interchange
        }

        return entitySelection.withReplaced(
            "ServiceJourneyInterchange",
            serviceJourneyInterchangeMap
        )
    }
}