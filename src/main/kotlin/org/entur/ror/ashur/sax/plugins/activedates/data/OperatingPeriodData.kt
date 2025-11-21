package org.entur.ror.ashur.sax.plugins.activedates.data

import org.entur.ror.ashur.sax.plugins.activedates.model.Period

data class OperatingPeriodData(
    var period: Period? = null,
    var fromDateId: String? = null,
    var toDateId: String? = null
)
