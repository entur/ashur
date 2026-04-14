package org.entur.ror.ashur.utils

fun removeRbPrefix(input: String): String {
    if (input.startsWith("rb_")) {
        return input.removePrefix("rb_")
    }
    if (input.startsWith("RB_")) {
        return input.removePrefix("RB_")
    }
    return input
}