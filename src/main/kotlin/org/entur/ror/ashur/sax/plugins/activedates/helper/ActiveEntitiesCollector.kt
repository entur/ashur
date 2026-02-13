package org.entur.ror.ashur.sax.plugins.activedates.helper

import org.entur.netex.tools.lib.extensions.putOrAddToSet
import org.entur.netex.tools.lib.model.NetexTypes

class ActiveEntitiesCollector {
    private val entities = mutableMapOf<String, MutableSet<String>>()

    init {
        // Initialize all entity types with empty sets
        entities[NetexTypes.DATED_SERVICE_JOURNEY] = mutableSetOf()
        entities[NetexTypes.SERVICE_JOURNEY] = mutableSetOf()
        entities[NetexTypes.DAY_TYPE_ASSIGNMENT] = mutableSetOf()
        entities[NetexTypes.DEAD_RUN] = mutableSetOf()
    }

    fun addDatedServiceJourney(id: String) = add(NetexTypes.DATED_SERVICE_JOURNEY, id)
    fun addServiceJourney(id: String) = add(NetexTypes.SERVICE_JOURNEY, id)
    fun addDayTypeAssignment(id: String) = add(NetexTypes.DAY_TYPE_ASSIGNMENT, id)
    fun addDeadRun(id: String) = add(NetexTypes.DEAD_RUN, id)

    private fun add(type: String, id: String) = entities.putOrAddToSet(type, id)

    fun serviceJourneys(): Set<String> = entities[NetexTypes.SERVICE_JOURNEY] ?: mutableSetOf()
    fun deadRuns(): Set<String> = entities[NetexTypes.DEAD_RUN] ?: mutableSetOf()

    // Additional getters for consistency
    fun datedServiceJourneys(): Set<String> = entities[NetexTypes.DATED_SERVICE_JOURNEY] ?: mutableSetOf()
    fun dayTypeAssignments(): Set<String> = entities[NetexTypes.DAY_TYPE_ASSIGNMENT] ?: mutableSetOf()

    fun toMap(): Map<String, Set<String>> = entities
}