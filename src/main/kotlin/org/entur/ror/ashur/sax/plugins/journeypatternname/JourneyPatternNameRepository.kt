package org.entur.ror.ashur.sax.plugins.journeypatternname

class JourneyPatternNameRepository(
    val routeNames: MutableMap<String, String> = mutableMapOf(),
    val journeyPatternRouteRefs: MutableMap<String, String> = mutableMapOf(),
    val journeyPatternsWithName: MutableSet<String> = mutableSetOf()
) {
    fun getRouteName(routeId: String): String? = routeNames[routeId]

    fun getRouteRefForJourneyPattern(journeyPatternId: String): String? =
        journeyPatternRouteRefs[journeyPatternId]

    fun hasExistingName(journeyPatternId: String): Boolean =
        journeyPatternsWithName.contains(journeyPatternId)

    fun getNameForJourneyPattern(journeyPatternId: String): String? {
        if (hasExistingName(journeyPatternId)) return null
        val routeId = getRouteRefForJourneyPattern(journeyPatternId) ?: return null
        return getRouteName(routeId)
    }
}
