package org.entur.ror.ashur.file

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InMemoryClaimStoreTest {

    @Test
    fun `createIfAbsent returns the new generation the first time and null when it already exists`() {
        val store = InMemoryClaimStore()
        assertNotNull(store.createIfAbsent("claims/RUT/c1", "first".toByteArray()))
        assertNull(store.createIfAbsent("claims/RUT/c1", "second".toByteArray()))
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
        val gen = store.createIfAbsent("claims/RUT/c1", "v1".toByteArray())!!

        assertNull(store.overwriteIfGeneration("claims/RUT/c1", "stale".toByteArray(), gen + 999))
        assertEquals("v1", String(store.read("claims/RUT/c1")!!.content))

        val newGen = store.overwriteIfGeneration("claims/RUT/c1", "v2".toByteArray(), gen)
        assertNotNull(newGen)
        val after = store.read("claims/RUT/c1")!!
        assertEquals("v2", String(after.content))
        assertEquals(newGen, after.generation)
        assertTrue(after.generation != gen)
    }

    @Test
    fun `overwriteIfGeneration returns null when the object does not exist`() {
        val store = InMemoryClaimStore()
        assertNull(store.overwriteIfGeneration("claims/RUT/missing", "x".toByteArray(), 1L))
    }
}
