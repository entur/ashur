package org.entur.ror.ashur.filter

import org.entur.netex.tools.lib.config.TimePeriod
import org.entur.netex.tools.lib.selectors.entities.EntitySelector
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesRepository
import org.entur.ror.ashur.sax.selectors.entities.BlockSelector

class IncludeBlocksAndRestrictedJourneysFilteringProfileConfig : BaseFilteringProfileConfig() {

    override val removePrivateData = false

    override fun includeElements() = listOf(
        "/PublicationDelivery/dataObjects/CompositeFrame/frames/VehicleScheduleFrame",
        "/PublicationDelivery/dataObjects/VehicleScheduleFrame"
    )

    override fun additionalEntitySelectors(
        activeDatesRepository: ActiveDatesRepository,
        timePeriod: TimePeriod
    ): List<EntitySelector> = listOf(BlockSelector())
}
