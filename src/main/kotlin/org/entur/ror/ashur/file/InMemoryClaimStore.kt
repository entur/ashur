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
        overwriteFailure?.let { throw it }
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

    /**
     * Test seam: while set, [overwriteIfGeneration] throws this instead of writing, so tests can
     * exercise the guard's fail-open paths (a real GCS 403/5xx).
     *
     * A seam on the fake rather than a Mockito bean override on purpose: an override changes the test
     * context cache key, which would give the owning test class its own ApplicationContext — and since
     * Spring caches contexts rather than closing them, a second Camel route would then consume the same
     * emulator subscription concurrently and steal other tests' messages.
     */
    @Volatile
    var overwriteFailure: RuntimeException? = null
}
