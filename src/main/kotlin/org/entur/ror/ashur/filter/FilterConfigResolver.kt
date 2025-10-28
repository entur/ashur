package org.entur.ror.ashur.filter

import org.entur.netex.tools.lib.config.FilterConfig
import org.springframework.stereotype.Component

@Component
class FilterConfigResolver(
    private val standardImportFilterConfig: StandardImportFilteringProfileConfig = StandardImportFilteringProfileConfig(),
    private val asIsImportFilterConfig: AsIsImportFilteringProfileConfig = AsIsImportFilteringProfileConfig(),
) {
    /**
     * Resolves the appropriate filter configuration based on the provided filtering profile.
     *
     * @param filterProfile The filtering profile to resolve a configuration for.
     * @return The corresponding filter profile configuration.
     */
    fun resolve(filterProfile: FilterProfile): FilterConfig {
        return when (filterProfile) {
            FilterProfile.StandardImportFilter -> standardImportFilterConfig.build()
            FilterProfile.AsIsImportFilter -> asIsImportFilterConfig.build()
        }
    }
}