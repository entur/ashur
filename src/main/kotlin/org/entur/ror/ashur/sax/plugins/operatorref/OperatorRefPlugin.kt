package org.entur.ror.ashur.sax.plugins.operatorref

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.plugin.AbstractNetexPlugin
import org.xml.sax.Attributes

class OperatorRefPlugin(
    val repository: OperatorRefRepository
) : AbstractNetexPlugin() {

    override fun getName(): String = "OperatorRefPlugin"

    override fun getDescription(): String =
        "Collects Line OperatorRef mappings and tracks which ServiceJourneys already have OperatorRef"

    override fun getSupportedElementTypes(): Set<String> = setOf("OperatorRef")

    override fun startElement(elementName: String, attributes: Attributes?, currentEntity: Entity?) {
        if (currentEntity == null) return
        val ref = attributes?.getValue("ref") ?: return
        when (currentEntity.type) {
            "Line" -> repository.lineOperatorRefs[currentEntity.id] = ref
            "ServiceJourney" -> repository.serviceJourneysWithOperatorRef.add(currentEntity.id)
        }
    }

    override fun getCollectedData(): OperatorRefRepository = repository
}
