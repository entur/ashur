package org.entur.ror.ashur.sax.plugins.servicejourneynaming

/**
 * Repository that stores collected data for ServiceJourney name injection.
 * Collects mappings during the plugin phase to be used by the handler phase.
 *
 * The data chain is:
 * ServiceJourney -> JourneyPatternRef -> JourneyPattern -> StopPointInJourneyPattern (order=1)
 *                -> DestinationDisplayRef -> DestinationDisplay -> FrontText
 */
data class ServiceJourneyNameRepository(
    /** Map: DestinationDisplay ID -> FrontText value */
    val destinationDisplayFrontText: MutableMap<String, String> = mutableMapOf(),

    /** Map: JourneyPattern ID -> DestinationDisplay ID (from first stop's DestinationDisplayRef) */
    val journeyPatternToDestinationDisplay: MutableMap<String, String> = mutableMapOf(),

    /** Map: ServiceJourney ID -> JourneyPattern ID (from JourneyPatternRef) */
    val serviceJourneyToJourneyPattern: MutableMap<String, String> = mutableMapOf(),

    /** Set: ServiceJourney IDs that already have a Name element */
    val serviceJourneysWithName: MutableSet<String> = mutableSetOf()
) {
    /**
     * Resolves the FrontText for a ServiceJourney by traversing the chain:
     * ServiceJourney -> JourneyPattern -> DestinationDisplay -> FrontText
     *
     * @param serviceJourneyId The ID of the ServiceJourney
     * @return FrontText value or null if chain cannot be resolved
     */
    fun getFrontTextForServiceJourney(serviceJourneyId: String): String? {
        val journeyPatternId = serviceJourneyToJourneyPattern[serviceJourneyId] ?: return null
        val destinationDisplayId = journeyPatternToDestinationDisplay[journeyPatternId] ?: return null
        return destinationDisplayFrontText[destinationDisplayId]
    }

    /**
     * Checks if a ServiceJourney already has a Name element.
     *
     * @param serviceJourneyId The ID of the ServiceJourney
     * @return true if the ServiceJourney already has a Name element
     */
    fun hasExistingName(serviceJourneyId: String): Boolean {
        return serviceJourneysWithName.contains(serviceJourneyId)
    }
}
