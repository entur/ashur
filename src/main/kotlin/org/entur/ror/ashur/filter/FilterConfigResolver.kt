package org.entur.ror.ashur.filter

import org.entur.netex.tools.lib.config.FilterConfig
import org.springframework.stereotype.Component

@Component
class FilterConfigResolver(
    private val standardImportFilterConfig: StandardImportFilteringProfileConfig = StandardImportFilteringProfileConfig(),
    private val asIsImportFilterConfig: AsIsImportFilteringProfileConfig = AsIsImportFilteringProfileConfig(),
    private val includeBlocksAndRestrictedJourneysFilterConfig: IncludeBlocksAndRestrictedJourneysFilteringProfileConfig = IncludeBlocksAndRestrictedJourneysFilteringProfileConfig(),
) {
    /**
     * Resolves the appropriate filter configuration based on the provided filtering profile.
     *
     * @param filterContext The context containing the filtering profile.
     * @return The corresponding filter profile configuration.
     */
    fun resolve(filterContext: FilterContext): FilterConfig {
        val filterProfile = filterContext.profile
        return when (filterProfile) {
            FilterProfile.StandardImportFilter -> standardImportFilterConfig.build(filterContext)
            FilterProfile.AsIsImportFilter -> asIsImportFilterConfig.build(filterContext)
            FilterProfile.IncludeBlocksAndRestrictedJourneysFilter -> includeBlocksAndRestrictedJourneysFilterConfig.build(filterContext)
        }
    }
}