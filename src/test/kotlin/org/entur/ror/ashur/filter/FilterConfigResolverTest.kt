package org.entur.ror.ashur.filter

import org.entur.ror.ashur.config.AppConfig
import org.entur.ror.ashur.exceptions.UnauthorizedFilterProfileException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FilterConfigResolverTest {
    private lateinit var appConfig: AppConfig
    private lateinit var resolver: FilterConfigResolver

    @BeforeEach
    fun setUp() {
        appConfig = AppConfig()
        appConfig.profileSecurity.blockExportAllowedCodespaces = listOf("allowed-codespace", "sof")
        appConfig.profileSecurity.asIsImportAllowedCodespaces = listOf("allowed-codespace")
        resolver = FilterConfigResolver(appConfig)
    }

    @Test
    fun testResolveStandardImportFilter() {
        val filterContext = FilterContext(profile = FilterProfile.StandardImportFilter, codespace = "TST")
        val config = resolver.resolve(filterContext)
        assertNotNull(config)
    }

    @Test
    fun testResolveStandardImportFilterAllowsAnyCodespace() {
        val filterContext = FilterContext(profile = FilterProfile.StandardImportFilter, codespace = "unauthorized-codespace")
        val config = resolver.resolve(filterContext)
        assertNotNull(config)
    }

    @Test
    fun testResolveAsIsImportFilterWithAllowedCodespace() {
        val filterContext = FilterContext(profile = FilterProfile.AsIsImportFilter, codespace = "allowed-codespace")
        val config = resolver.resolve(filterContext)
        assertNotNull(config)
    }

    @Test
    fun testResolveAsIsImportFilterWithUnauthorizedCodespaceThrowsException() {
        val filterContext = FilterContext(profile = FilterProfile.AsIsImportFilter, codespace = "unauthorized-codespace")
        val exception = assertThrows(UnauthorizedFilterProfileException::class.java) {
            resolver.resolve(filterContext)
        }
        assertTrue(exception.message!!.contains("unauthorized-codespace"))
        assertTrue(exception.message!!.contains("as-is import"))
    }

    @Test
    fun testResolveIncludeBlocksFilterWithAllowedCodespace() {
        val filterContext = FilterContext(
            profile = FilterProfile.IncludeBlocksAndRestrictedJourneysFilter,
            codespace = "allowed-codespace"
        )
        val config = resolver.resolve(filterContext)
        assertNotNull(config)
    }

    @Test
    fun testResolveIncludeBlocksFilterWithUnauthorizedCodespaceThrowsException() {
        val filterContext = FilterContext(
            profile = FilterProfile.IncludeBlocksAndRestrictedJourneysFilter,
            codespace = "unauthorized-codespace"
        )
        val exception = assertThrows(UnauthorizedFilterProfileException::class.java) {
            resolver.resolve(filterContext)
        }
        assertTrue(exception.message!!.contains("unauthorized-codespace"))
        assertTrue(exception.message!!.contains("block export"))
    }

    @Test
    fun testFilterConfigsAreRebuiltWhenResolved() {
        val filterContext = FilterContext(profile = FilterProfile.StandardImportFilter, codespace = "TST")
        val config1 = resolver.resolve(filterContext)
        val config2 = resolver.resolve(filterContext)
        assertNotSame(config1, config2, "Expected different instances for each resolve call")
    }
}