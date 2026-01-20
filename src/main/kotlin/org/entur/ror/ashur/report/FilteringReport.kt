package org.entur.ror.ashur.report

import java.time.LocalDateTime

data class FilteringReport(
    val created: LocalDateTime,
    val correlationId: String,
    val codespace: String,
    val status: String,
    val reason: String?,
)