package org.entur.ror.ashur.filter

import org.entur.netex.tools.lib.config.FilterConfig
import org.entur.netex.tools.lib.config.FilterConfigBuilder
import org.entur.netex.tools.lib.config.TimePeriod
import java.time.LocalDate

class StandardImportFilteringProfileConfig: FilterProfileConfiguration {
    override fun build(): FilterConfig =
        FilterConfigBuilder()
            .withPeriod(TimePeriod(
                start = LocalDate.now().minusDays(2),
                end = null
            ))
            .withSkipElements(listOf("VehicleScheduleFrame", "DeadRun"))
            .withRemovePrivateData(true)
            .withPreserveComments(true)
            .withUseSelfClosingTagsWhereApplicable(false)
            .withPruneReferences(true)
            .withReferencesToExcludeFromPruning(setOf("QuayRef"))
            .withUnreferencedEntitiesToPrune(
                setOf(
                    "JourneyPattern",
                    "Route",
                    "Line"
                )
            )
            .build()
}