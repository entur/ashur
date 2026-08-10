package org.entur.ror.ashur.file

/**
 * Minimal coordination primitive for the redelivery guard. Backed in production by GCS object
 * generation preconditions; in tests/local by an in-memory map. Only the three operations the guard
 * needs are exposed — deliberately narrower than a blob store, and NOT built on [BlobStoreRepository]
 * because that abstraction cannot express `ifGenerationMatch`.
 */
interface ClaimStore {
    /** Atomically create [name] iff it does not already exist. @return true if we created it. */
    fun createIfAbsent(name: String, content: ByteArray): Boolean

    /** @return the current content + generation of [name], or null if it does not exist. */
    fun read(name: String): VersionedClaim?

    /** Atomically overwrite [name] iff its current generation equals [expectedGeneration]. @return true if we won. */
    fun overwriteIfGeneration(name: String, content: ByteArray, expectedGeneration: Long): Boolean
}

/** A claim object's bytes together with the generation they were read at. */
class VersionedClaim(val content: ByteArray, val generation: Long)
