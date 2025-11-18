package org.entur.ror.ashur.sax.plugins.activedates.model

import java.time.LocalDate

data class Period(
    val fromDate: LocalDate?,
    val toDate: LocalDate?,
)