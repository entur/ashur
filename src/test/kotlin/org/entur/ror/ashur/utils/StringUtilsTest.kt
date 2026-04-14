package org.entur.ror.ashur.utils

import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

class StringUtilsTest {
    @Test
    fun `removeRbPrefix should remove 'rb_' prefix when present`() {
        val input = "rb_exampleString"
        val expected = "exampleString"
        val result = removeRbPrefix(input)
        assertEquals(expected, result)
    }

    @Test
    fun `removeRbPrefix should remove 'RB_' prefix when present`() {
        val input = "RB_exampleString"
        val expected = "exampleString"
        val result = removeRbPrefix(input)
        assertEquals(expected, result)
    }
}