package org.entur.ror.ashur.filter

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class FilterConfigResolverTest {
    @Test
    fun testResolveStandardImportFilter() {
        val resolver = FilterConfigResolver()
        val config = resolver.resolve(FilterProfile.StandardImportFilter)
        assertNotNull(config)
    }

    @Test
    fun testResolveAsIsImportFilter() {
        val resolver = FilterConfigResolver()
        val config = resolver.resolve(FilterProfile.AsIsImportFilter)
        assertNotNull(config)
    }

    @Test
    fun testFilterConfigsAreRebuiltWhenResolved() {
        val resolver = FilterConfigResolver()
        val config1 = resolver.resolve(FilterProfile.StandardImportFilter)
        val config2 = resolver.resolve(FilterProfile.StandardImportFilter)
        assertNotSame(config1, config2, "Expected different instances for each resolve call")
    }
}