package org.entur.ror.ashur

import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.support.DefaultExchange
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class ExtensionsTest {

    private fun exchange() = DefaultExchange(DefaultCamelContext())

    @Test
    fun `getPubsubAttributes returns string map when header is a valid map`() {
        val exchange = exchange()
        exchange.getIn().setHeader(
            "CamelGooglePubsubAttributes",
            mapOf("key1" to "value1", "key2" to "value2"),
        )

        val result = exchange.getPubsubAttributes()

        assertEquals(mapOf("key1" to "value1", "key2" to "value2"), result)
    }

    @Test
    fun `getPubsubAttributes converts non-string values to strings`() {
        val exchange = exchange()
        exchange.getIn().setHeader(
            "CamelGooglePubsubAttributes",
            mapOf("intKey" to 42, "boolKey" to true),
        )

        val result = exchange.getPubsubAttributes()

        assertEquals(mapOf("intKey" to "42", "boolKey" to "true"), result)
    }

    @Test
    fun `getPubsubAttributes throws when header is not a map`() {
        val exchange = exchange()
        exchange.getIn().setHeader("CamelGooglePubsubAttributes", "not-a-map")

        assertThrows(IllegalArgumentException::class.java) {
            exchange.getPubsubAttributes()
        }
    }

    @Test
    fun `getPubsubAttributes throws when header is absent`() {
        val exchange = exchange()

        assertThrows(IllegalArgumentException::class.java) {
            exchange.getPubsubAttributes()
        }
    }

    @Test
    fun `getPubsubAttributes throws when map is empty`() {
        val exchange = exchange()
        exchange.getIn().setHeader("CamelGooglePubsubAttributes", emptyMap<String, String>())

        assertThrows(IllegalArgumentException::class.java) {
            exchange.getPubsubAttributes()
        }
    }
}
