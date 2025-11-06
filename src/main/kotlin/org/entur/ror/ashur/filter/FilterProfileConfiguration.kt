package org.entur.ror.ashur.filter

import org.entur.netex.tools.lib.config.FilterConfig

interface FilterProfileConfiguration {
    fun build(codespace: String): FilterConfig
}
