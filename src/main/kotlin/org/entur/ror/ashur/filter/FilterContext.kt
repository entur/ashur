package org.entur.ror.ashur.filter

import java.time.LocalDateTime

data class FilterContext(
    val profile: FilterProfile,
    val codespace: String,
    val fileCreatedAt: LocalDateTime? = null,
)