package org.entur.ror.ashur.sax.plugins.operatorref

class OperatorRefRepository(
    val lineOperatorRefs: MutableMap<String, String> = mutableMapOf(),
    val serviceJourneysWithOperatorRef: MutableSet<String> = mutableSetOf()
) {
    fun getOperatorRefForLine(lineId: String): String? = lineOperatorRefs[lineId]

    fun hasOperatorRef(serviceJourneyId: String): Boolean =
        serviceJourneysWithOperatorRef.contains(serviceJourneyId)
}
