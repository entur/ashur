package org.entur.ror.ashur.filter

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FilterConfigResolverTest {
    private lateinit var resolver: FilterConfigResolver

    @BeforeEach
    fun setUp() {
        resolver = FilterConfigResolver()
    }

    @Test
    fun testResolveStandardImportFilter() {
        val filterContext = FilterContext(profile = FilterProfile.StandardImportFilter, codespace = "TST")
        val config = resolver.resolve(filterContext)
        assertNotNull(config)
    }

    @Test
    fun testResolveAsIsImportFilter() {
        val filterContext = FilterContext(profile = FilterProfile.AsIsImportFilter, codespace = "TST")
        val config = resolver.resolve(filterContext)
        assertNotNull(config)
    }

    @Test
    fun testResolveIncludeBlocksFilter() {
        val filterContext = FilterContext(
            profile = FilterProfile.IncludeBlocksAndRestrictedJourneysFilter,
            codespace = "TST"
        )
        val config = resolver.resolve(filterContext)
        assertNotNull(config)
    }

    @Test
    fun testFilterConfigsAreRebuiltWhenResolved() {
        val filterContext = FilterContext(profile = FilterProfile.StandardImportFilter, codespace = "TST")
        val config1 = resolver.resolve(filterContext)
        val config2 = resolver.resolve(filterContext)
        assertNotSame(config1, config2, "Expected different instances for each resolve call")
    }
}
