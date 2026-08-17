package org.entur.ror.ashur.file

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageException
import org.entur.ror.ashur.config.AppConfig
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

/**
 * GCS-backed [ClaimStore] using object generation preconditions on the internal Ashur bucket:
 * - create-if-absent via [Storage.BlobTargetOption.doesNotExist] (`ifGenerationMatch=0`),
 * - compare-and-swap via [Storage.BlobTargetOption.generationMatch].
 *
 * A `412 PRECONDITION FAILED` is the *expected* "someone else got there first" signal and is
 * translated to a boolean; any other [StorageException] is rethrown so the guard can fail open.
 */
@Component
@Profile("gcp")
class GcsClaimStore(
    private val storage: Storage,
    appConfig: AppConfig,
) : ClaimStore {

    private val bucket: String = appConfig.gcp.ashurBucketName

    private companion object {
        const val PRECONDITION_FAILED = 412
        const val CONTENT_TYPE = "application/json"
    }

    override fun createIfAbsent(name: String, content: ByteArray): Long? =
        writeWithPrecondition(name, content, Storage.BlobTargetOption.doesNotExist())

    override fun read(name: String): VersionedClaim? {
        val blob = storage.get(BlobId.of(bucket, name)) ?: return null
        return VersionedClaim(blob.getContent(), blob.generation)
    }

    override fun overwriteIfGeneration(name: String, content: ByteArray, expectedGeneration: Long): Long? =
        writeWithPrecondition(name, content, Storage.BlobTargetOption.generationMatch(expectedGeneration))

    private fun writeWithPrecondition(name: String, content: ByteArray, option: Storage.BlobTargetOption): Long? {
        val blobInfo = BlobInfo.newBuilder(BlobId.of(bucket, name)).setContentType(CONTENT_TYPE).build()
        return try {
            storage.create(blobInfo, content, option).generation
        } catch (e: StorageException) {
            if (e.code != PRECONDITION_FAILED) throw e
            null
        }
    }
}
