package org.entur.ror.ashur.file

import com.google.cloud.storage.Blob
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageException
import org.entur.ror.ashur.config.AppConfig
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GcsClaimStoreTest {

    private val bucket = "ror-ashur-internal-gcp-test"
    private val appConfig = AppConfig(
        gcp = AppConfig.GcpConfig().also { it.ashurBucketName = bucket }
    )

    private fun storeWith(storage: Storage) = GcsClaimStore(storage, appConfig)

    @Test
    fun `createIfAbsent returns the new generation when the create succeeds`() {
        val blob = mock<Blob> { on { getGeneration() } doReturn 7L }
        val storage = mock<Storage> {
            on { create(any<BlobInfo>(), any<ByteArray>(), any<Storage.BlobTargetOption>()) } doReturn blob
        }
        assertEquals(7L, storeWith(storage).createIfAbsent("claims/RUT/c1", "x".toByteArray()))
    }

    @Test
    fun `createIfAbsent returns null on a 412 precondition failure`() {
        val storage = mock<Storage> {
            on { create(any<BlobInfo>(), any<ByteArray>(), any<Storage.BlobTargetOption>()) } doThrow StorageException(412, "precondition")
        }
        assertNull(storeWith(storage).createIfAbsent("claims/RUT/c1", "x".toByteArray()))
    }

    @Test
    fun `createIfAbsent rethrows non-412 storage errors so the guard can fail open`() {
        val storage = mock<Storage> {
            on { create(any<BlobInfo>(), any<ByteArray>(), any<Storage.BlobTargetOption>()) } doThrow StorageException(503, "unavailable")
        }
        assertThrows<StorageException> { storeWith(storage).createIfAbsent("claims/RUT/c1", "x".toByteArray()) }
    }

    @Test
    fun `read returns null when the blob is absent`() {
        val storage = mock<Storage> {
            on { get(any<BlobId>()) } doReturn null
        }
        assertNull(storeWith(storage).read("claims/RUT/c1"))
    }

    @Test
    fun `read returns content and generation when present`() {
        val blob = mock<Blob> {
            on { getContent() } doReturn "payload".toByteArray()
            on { getGeneration() } doReturn 42L
        }
        val storage = mock<Storage> {
            on { get(eq(BlobId.of(bucket, "claims/RUT/c1"))) } doReturn blob
        }
        val versioned = storeWith(storage).read("claims/RUT/c1")!!
        assertEquals("payload", String(versioned.content))
        assertEquals(42L, versioned.generation)
    }

    @Test
    fun `overwriteIfGeneration returns the new generation on success and null on 412`() {
        val blob = mock<Blob> { on { getGeneration() } doReturn 8L }
        val ok = mock<Storage> {
            on { create(any<BlobInfo>(), any<ByteArray>(), any<Storage.BlobTargetOption>()) } doReturn blob
        }
        assertEquals(8L, storeWith(ok).overwriteIfGeneration("claims/RUT/c1", "x".toByteArray(), 7L))

        val conflict = mock<Storage> {
            on { create(any<BlobInfo>(), any<ByteArray>(), any<Storage.BlobTargetOption>()) } doThrow StorageException(412, "precondition")
        }
        assertNull(storeWith(conflict).overwriteIfGeneration("claims/RUT/c1", "x".toByteArray(), 7L))
    }
}
