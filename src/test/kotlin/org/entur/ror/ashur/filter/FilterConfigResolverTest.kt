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
}