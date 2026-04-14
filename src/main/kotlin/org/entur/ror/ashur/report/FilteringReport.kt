package org.entur.ror.ashur.report

import java.time.LocalDateTime

data class FilteringReport(
    val created: LocalDateTime,
    val correlationId: String,
    val codespace: String,
    val filterProfile: String?,
    val status: String,
    val reason: String?,
    val entityTypeCounts: Map<String, Int>?,
)