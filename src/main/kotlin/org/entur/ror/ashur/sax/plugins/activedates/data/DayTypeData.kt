package org.entur.ror.ashur.sax.plugins.activedates.data

import java.time.DayOfWeek
import java.time.LocalDate

data class DayTypeData(
    val operatingPeriods: MutableList<String> = mutableListOf(),
    val dates: MutableList<LocalDate> = mutableListOf(),
    val operatingDays: MutableList<String> = mutableListOf(),
    val daysOfWeek: MutableSet<DayOfWeek> = mutableSetOf(),
    val excludedDates: MutableSet<LocalDate> = mutableSetOf(),
    val excludedOperatingDays: MutableSet<String> = mutableSetOf(),
    val excludedOperatingPeriods: MutableSet<String> = mutableSetOf(),
)
