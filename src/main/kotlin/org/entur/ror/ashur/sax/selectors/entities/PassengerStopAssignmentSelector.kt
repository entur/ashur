package org.entur.ror.ashur.sax.selectors.entities

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.netex.tools.lib.selections.EntitySelection
import org.entur.netex.tools.lib.selectors.entities.EntitySelector

/**
 * Filters PassengerStopAssignment and ScheduledStopPoint entities when the only reference made to a
 * ScheduledStopPoint is made from a PassengerStopAssignment in the provided entity selection. In these cases,
 * both the PassengerStopAssignment and ScheduledStopPoint will be removed from the EntitySelection returned from
 * selectEntities.
 * */
class PassengerStopAssignmentSelector: EntitySelector {
    fun excludePassengerStopAssignments(entities: Collection<Entity>): Collection<Entity> =
        entities.filter { it.type !== NetexTypes.PASSENGER_STOP_ASSIGNMENT }

    fun scheduledStopPointIsReferredToInSelection(
        scheduledStopPointId: String?,
        model: EntityModel,
        entitySelection: EntitySelection,
    ): Boolean {
        if (scheduledStopPointId == null) {
            return false
        }

        val referringEntities = model.getEntitiesReferringTo(scheduledStopPointId)
        val referringEntitiesExcludingAssignments = excludePassengerStopAssignments(referringEntities)
        val referringEntitiesInSelection = referringEntitiesExcludingAssignments.filter { entitySelection.includes(it.id) }
        return referringEntitiesInSelection.isNotEmpty()
    }

    fun findPassengerStopAssignmentsToKeep(model: EntityModel, entitySelection: EntitySelection): List<Entity> {
        val passengerStopAssignments = model.getEntitiesOfType(NetexTypes.PASSENGER_STOP_ASSIGNMENT)
        return passengerStopAssignments.filter { passengerStopAssignment ->
            val psaId = passengerStopAssignment.id
            val scheduledStopPointRef = model.getRefsOfTypeFrom(psaId, "ScheduledStopPointRef").firstOrNull()?.ref
            scheduledStopPointIsReferredToInSelection(scheduledStopPointRef, model, entitySelection)
        }
    }

    fun findScheduledStopPointsToKeep(model: EntityModel, entitySelection: EntitySelection): List<Entity> {
        val scheduledStopPoints = model.getEntitiesOfType(NetexTypes.SCHEDULED_STOP_POINT)
        return scheduledStopPoints.filter { scheduledStopPoint ->
            scheduledStopPointIsReferredToInSelection(scheduledStopPoint.id, model, entitySelection)
        }
    }

    override fun selectEntities(model: EntityModel, currentEntitySelection: EntitySelection?): EntitySelection {
        val entitySelection = currentEntitySelection!!.copy()

        val passengerStopAssignmentsToKeep = findPassengerStopAssignmentsToKeep(model, entitySelection)
        val passengerStopAssignmentMap = mutableMapOf<String, Entity>()
        passengerStopAssignmentsToKeep.forEach { psaToKeep ->
            passengerStopAssignmentMap[psaToKeep.id] = psaToKeep
        }

        val scheduledStopPointsToKeep = findScheduledStopPointsToKeep(model, currentEntitySelection)
        val scheduledStopPointsMap = mutableMapOf<String, Entity>()
        scheduledStopPointsToKeep.forEach { sspToKeep ->
            scheduledStopPointsMap[sspToKeep.id] = sspToKeep
        }

        return entitySelection
            .withReplaced(NetexTypes.PASSENGER_STOP_ASSIGNMENT, passengerStopAssignmentMap)
            .withReplaced(NetexTypes.SCHEDULED_STOP_POINT, scheduledStopPointsMap)
    }
}