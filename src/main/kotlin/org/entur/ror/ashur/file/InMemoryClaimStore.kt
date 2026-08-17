package org.entur.ror.ashur.file

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

/**
 * Faithful in-memory [ClaimStore] modelling GCS's atomic create-if-absent and generation-match
 * semantics. Used as the `local`/`test` bean and directly as the fake in guard unit tests.
 *
 * A single Ashur process in local/test has no cross-pod race to coordinate, so this is enough there;
 * production coordination across the two pods is [GcsClaimStore]'s job.
 */
@Component
@Profile("local", "test")
class InMemoryClaimStore : ClaimStore {

    private class Entry(val content: ByteArray, val generation: Long)

    private val entries = HashMap<String, Entry>()
    private var generationSequence = 0L

    @Synchronized
    override fun createIfAbsent(name: String, content: ByteArray): Long? {
        if (entries.containsKey(name)) return null
        val generation = ++generationSequence
        entries[name] = Entry(content.copyOf(), generation)
        return generation
    }

    @Synchronized
    override fun read(name: String): VersionedClaim? {
        val entry = entries[name] ?: return null
        return VersionedClaim(entry.content.copyOf(), entry.generation)
    }

    @Synchronized
    override fun overwriteIfGeneration(name: String, content: ByteArray, expectedGeneration: Long): Long? {
        val entry = entries[name] ?: return null
        if (entry.generation != expectedGeneration) return null
        val generation = ++generationSequence
        entries[name] = Entry(content.copyOf(), generation)
        return generation
    }

    /** Test seam: seed a claim directly (used by the route integration test to force a bounce). */
    @Synchronized
    fun put(name: String, content: ByteArray) {
        entries[name] = Entry(content.copyOf(), ++generationSequence)
    }
}
