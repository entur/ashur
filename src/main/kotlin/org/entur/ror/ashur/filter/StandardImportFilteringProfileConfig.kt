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
                end = LocalDate.now().plusYears(1)
            ))
            .withSkipElements(
                listOf(
                    "VehicleScheduleFrame",
                    "DeadRun",
                    "SiteFrame",
                    "DataSource",
                    "TrainComponent",
                    "TrainNumber",
                    "Train",
                    "TrainInCompoundTrain",
                    "CompoundTrain",
                    "JourneyPart"
                )
            )
            .withRemovePrivateData(true)
            .withPreserveComments(true)
            .withUseSelfClosingTagsWhereApplicable(false)
            .withPruneReferences(true)
            .withReferencesToExcludeFromPruning(setOf("QuayRef"))
            .withUnreferencedEntitiesToPrune(
                setOf(
                    "JourneyPattern",
                    "Route",
                    "Network",
                    "Line",
                    "Operator",
                )
            )
            .build()
}