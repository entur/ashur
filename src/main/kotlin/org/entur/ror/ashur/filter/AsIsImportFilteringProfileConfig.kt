package org.entur.ror.ashur.filter

import org.entur.netex.tools.lib.config.FilterConfig
import org.entur.netex.tools.lib.config.FilterConfigBuilder

class AsIsImportFilteringProfileConfig: FilterProfileConfiguration {
    override fun build(): FilterConfig =
        FilterConfigBuilder()
            .withSkipElements(listOf())
            .withRemovePrivateData(false)
            .withPreserveComments(true)
            .withPruneReferences(false)
            .withReferencesToExcludeFromPruning(setOf())
            .withUnreferencedEntitiesToPrune(setOf())
            .withUseSelfClosingTagsWhereApplicable(false)
            .build()
}