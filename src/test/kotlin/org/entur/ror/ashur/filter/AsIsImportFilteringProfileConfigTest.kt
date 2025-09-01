package org.entur.ror.ashur.filter

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class AsIsImportFilteringProfileConfigTest {
    @Test
    fun testAsIsImportFilteringProfileConfig() {
        val config = AsIsImportFilteringProfileConfig().build()
        assertNotNull(config)
        assertTrue(config.skipElements.isEmpty())
        assertTrue(config.referencesToExcludeFromPruning.isEmpty())
        assertTrue(config.unreferencedEntitiesToPrune.isEmpty())
        assertTrue(config.preserveComments)
        assertFalse(config.removePrivateData)
        assertFalse(config.pruneReferences)
        assertFalse(config.useSelfClosingTagsWhereApplicable)
    }
}