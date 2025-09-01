package org.entur.ror.ashur.filter

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertFalse
import kotlin.test.assertNull

class StandardImportFilteringProfileConfigTest {
    @Test
    fun testStandardImportFilteringProfileConfig() {
        val config = StandardImportFilteringProfileConfig().build()
        assertTrue(config.period.start!!.isEqual(LocalDate.now().minusDays(2)))
        assertNull(config.period.end)
        assertTrue(config.skipElements.containsAll(listOf("VehicleScheduleFrame", "DeadRun")))
        assertTrue(config.removePrivateData)
        assertTrue(config.preserveComments)
        assertTrue(config.pruneReferences)
        assertTrue(config.referencesToExcludeFromPruning.contains("QuayRef"))
        assertTrue(config.unreferencedEntitiesToPrune.containsAll(listOf("JourneyPattern", "Route", "Line")))
        assertFalse(config.useSelfClosingTagsWhereApplicable)
    }
}