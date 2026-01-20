package org.entur.ror.ashur.report

import org.threeten.bp.LocalDateTime

data class FilteringReport(
    val created: LocalDateTime,
    val correlationId: String,
    val codespace: String,
    val status: String,
    val reason: String?,
)