package org.entur.ror.ashur.filter

import org.entur.netex.tools.lib.config.FilterConfig
import org.entur.ror.ashur.config.AppConfig
import org.entur.ror.ashur.exceptions.UnauthorizedFilterProfileException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class FilterConfigResolver(
    private val appConfig: AppConfig,
    private val standardImportFilterConfig: StandardImportFilteringProfileConfig = StandardImportFilteringProfileConfig(),
    private val asIsImportFilterConfig: AsIsImportFilteringProfileConfig = AsIsImportFilteringProfileConfig(),
    private val includeBlocksAndRestrictedJourneysFilterConfig: IncludeBlocksAndRestrictedJourneysFilteringProfileConfig = IncludeBlocksAndRestrictedJourneysFilteringProfileConfig(),
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Resolves the appropriate filter configuration based on the provided filtering profile.
     *
     * @param filterContext The context containing the filtering profile.
     * @return The corresponding filter profile configuration.
     */
    fun resolve(filterContext: FilterContext): FilterConfig {
        val filterProfile = filterContext.profile
        val codespace = filterContext.codespace

        validateProfileAuthorization(filterProfile, codespace)

        return when (filterProfile) {
            FilterProfile.StandardImportFilter -> standardImportFilterConfig.build(filterContext)
            FilterProfile.AsIsImportFilter -> asIsImportFilterConfig.build(filterContext)
            FilterProfile.IncludeBlocksAndRestrictedJourneysFilter -> includeBlocksAndRestrictedJourneysFilterConfig.build(filterContext)
        }
    }

    private fun validateProfileAuthorization(profile: FilterProfile, codespace: String) {
        val (allowedCodespaces, profileDescription) = when (profile) {
            FilterProfile.IncludeBlocksAndRestrictedJourneysFilter ->
                appConfig.profileSecurity.blockExportAllowedCodespaces to "block export"
            FilterProfile.AsIsImportFilter ->
                appConfig.profileSecurity.asIsImportAllowedCodespaces to "as-is import (private data)"
            FilterProfile.StandardImportFilter -> return // Always allowed
        }

        if (codespace !in allowedCodespaces) {
            logger.warn(
                "Rejected unauthorized filter profile request: " +
                "profile=$profile, codespace=$codespace, " +
                "allowedCodespaces=$allowedCodespaces"
            )
            throw UnauthorizedFilterProfileException(
                "Codespace '$codespace' is not authorized for $profileDescription. " +
                "Allowed codespaces: ${allowedCodespaces.joinToString(", ")}"
            )
        }

        logger.info("Authorized filter profile request: profile=$profile, codespace=$codespace")
    }
}