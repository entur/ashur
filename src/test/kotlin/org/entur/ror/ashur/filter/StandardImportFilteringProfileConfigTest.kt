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
        assertTrue(config.period.end!!.isEqual(LocalDate.now().plusYears(1)))
        assertTrue(config.skipElements.containsAll(
            listOf(
                "VehicleScheduleFrame",
                "DeadRun",
                "SiteFrame",
                "DataSource",
                "TrainComponent",
                "TrainElement",
                "TrainNumber",
                "Train",
                "TrainInCompoundTrain",
                "CompoundTrain",
                "JourneyPart"
            )
        ))
        assertTrue(config.removePrivateData)
        assertFalse(config.preserveComments)
        assertTrue(config.pruneReferences)
        assertTrue(config.referencesToExcludeFromPruning.contains("QuayRef"))
        assertTrue(config.unreferencedEntitiesToPrune.containsAll(
            listOf(
                "JourneyPattern",
                "Route",
                "Network",
                "Line",
                "Operator",
            ))
        )
        assertFalse(config.useSelfClosingTagsWhereApplicable)
    }
}