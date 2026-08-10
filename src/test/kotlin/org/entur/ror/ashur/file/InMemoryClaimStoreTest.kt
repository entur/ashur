package org.entur.ror.ashur.file

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InMemoryClaimStoreTest {

    @Test
    fun `createIfAbsent returns true the first time and false when it already exists`() {
        val store = InMemoryClaimStore()
        assertTrue(store.createIfAbsent("claims/RUT/c1", "first".toByteArray()))
        assertFalse(store.createIfAbsent("claims/RUT/c1", "second".toByteArray()))
    }

    @Test
    fun `read returns null when absent and content plus generation when present`() {
        val store = InMemoryClaimStore()
        assertNull(store.read("claims/RUT/c1"))

        store.createIfAbsent("claims/RUT/c1", "payload".toByteArray())
        val versioned = store.read("claims/RUT/c1")!!

        assertEquals("payload", String(versioned.content))
        assertTrue(versioned.generation > 0)
    }

    @Test
    fun `overwriteIfGeneration succeeds only when the expected generation matches`() {
        val store = InMemoryClaimStore()
        store.createIfAbsent("claims/RUT/c1", "v1".toByteArray())
        val gen = store.read("claims/RUT/c1")!!.generation

        assertFalse(store.overwriteIfGeneration("claims/RUT/c1", "stale".toByteArray(), gen + 999))
        assertEquals("v1", String(store.read("claims/RUT/c1")!!.content))

        assertTrue(store.overwriteIfGeneration("claims/RUT/c1", "v2".toByteArray(), gen))
        val after = store.read("claims/RUT/c1")!!
        assertEquals("v2", String(after.content))
        assertTrue(after.generation != gen)
    }

    @Test
    fun `overwriteIfGeneration returns false when the object does not exist`() {
        val store = InMemoryClaimStore()
        assertFalse(store.overwriteIfGeneration("claims/RUT/missing", "x".toByteArray(), 1L))
    }
}
