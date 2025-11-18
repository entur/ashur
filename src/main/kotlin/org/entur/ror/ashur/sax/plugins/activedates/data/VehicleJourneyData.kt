package org.entur.ror.ashur.sax.plugins.activedates.data

import java.time.LocalTime

data class VehicleJourneyData(
    val dayTypes: MutableList<String> = mutableListOf(),
    val operatingDays: MutableList<String> = mutableListOf(),
    var finalArrivalTime: LocalTime? = null,
    var finalArrivalDayOffset: Long = 0
)
