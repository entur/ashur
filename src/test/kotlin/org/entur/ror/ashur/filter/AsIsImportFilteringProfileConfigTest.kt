package org.entur.ror.ashur.filter

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

class AsIsImportFilteringProfileConfigTest {
    @Test
    fun testAsIsImportFilteringProfileConfig() {
        val filterContext = FilterContext(codespace = "TST", profile = FilterProfile.AsIsImportFilter)
        val config = AsIsImportFilteringProfileConfig().build(filterContext)
        assertNotNull(config)
        Assertions.assertTrue(config.skipElements.isEmpty())
        Assertions.assertTrue(config.referencesToExcludeFromPruning.isEmpty())
        Assertions.assertTrue(config.unreferencedEntitiesToPrune.isEmpty())
        Assertions.assertTrue(config.preserveComments)
        Assertions.assertFalse(config.removePrivateData)
        Assertions.assertFalse(config.pruneReferences)
        Assertions.assertFalse(config.useSelfClosingTagsWhereApplicable)
    }
}