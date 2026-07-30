package org.entur.ror.ashur

import com.google.pubsub.v1.PubsubMessage
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.support.DefaultExchange
import org.entur.ror.ashur.exceptions.InvalidFilterProfileException
import org.entur.ror.ashur.filter.FilterProfile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

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

    // getFilterProfile

    @Test
    fun `getFilterProfile returns correct enum for valid attribute`() {
        val message = pubsubMessage("EnturFilteringProfile" to "StandardImportFilter")

        assertEquals(FilterProfile.StandardImportFilter, message.getFilterProfile())
    }

    @Test
    fun `getFilterProfile throws when attribute is missing`() {
        val message = pubsubMessage()

        assertThrows(InvalidFilterProfileException::class.java) {
            message.getFilterProfile()
        }
    }

    @Test
    fun `getFilterProfile throws when attribute is not a valid profile`() {
        val message = pubsubMessage("EnturFilteringProfile" to "NotAProfile")

        assertThrows(InvalidFilterProfileException::class.java) {
            message.getFilterProfile()
        }
    }

    // getFileCreatedTimestamp

    @Test
    fun `getFileCreatedTimestamp returns parsed timestamp when attribute is present`() {
        val timestamp = LocalDateTime.of(2025, 6, 15, 12, 30, 0)
        val message = pubsubMessage("FileCreatedTimestamp" to timestamp.toString())

        assertEquals(timestamp, message.getFileCreatedTimestamp())
    }

    @Test
    fun `getFileCreatedTimestamp returns null when attribute is absent`() {
        val message = pubsubMessage()

        assertNull(message.getFileCreatedTimestamp())
    }

    @Test
    fun `getFileCreatedTimestamp throws when attribute is not a valid timestamp`() {
        val message = pubsubMessage("FileCreatedTimestamp" to "not-a-date")

        assertThrows(DateTimeParseException::class.java) {
            message.getFileCreatedTimestamp()
        }
    }

    // addPubsubAttribute

    @Test
    fun `addPubsubAttribute adds to existing attributes`() {
        val exchange = exchange()
        exchange.getIn().setHeader("CamelGooglePubsubAttributes", mutableMapOf("existing" to "value"))

        exchange.addPubsubAttribute("newKey", "newValue")

        @Suppress("UNCHECKED_CAST")
        val attributes = exchange.getIn().getHeader("CamelGooglePubsubAttributes", Map::class.java) as Map<String, String>
        assertEquals("value", attributes["existing"])
        assertEquals("newValue", attributes["newKey"])
    }

    @Test
    fun `addPubsubAttribute creates new map when header is absent`() {
        val exchange = exchange()

        exchange.addPubsubAttribute("key", "value")

        @Suppress("UNCHECKED_CAST")
        val attributes = exchange.getIn().getHeader("CamelGooglePubsubAttributes", Map::class.java) as Map<String, String>
        assertEquals("value", attributes["key"])
    }

    // helpers

    private fun pubsubMessage(vararg attributes: Pair<String, String>): PubsubMessage =
        PubsubMessage.newBuilder().putAllAttributes(mapOf(*attributes)).build()
}
