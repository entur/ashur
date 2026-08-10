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
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GcsClaimStoreTest {

    private val bucket = "ror-ashur-internal-gcp-test"
    private val appConfig = AppConfig(
        gcp = AppConfig.GcpConfig().also { it.ashurBucketName = bucket }
    )

    private fun storeWith(storage: Storage) = GcsClaimStore(storage, appConfig)

    @Test
    fun `createIfAbsent returns true when the create succeeds`() {
        val storage = mock<Storage> {
            on { create(any<BlobInfo>(), any<ByteArray>(), any<Storage.BlobTargetOption>()) } doReturn mock<Blob>()
        }
        assertTrue(storeWith(storage).createIfAbsent("claims/RUT/c1", "x".toByteArray()))
    }

    @Test
    fun `createIfAbsent returns false on a 412 precondition failure`() {
        val storage = mock<Storage> {
            on { create(any<BlobInfo>(), any<ByteArray>(), any<Storage.BlobTargetOption>()) } doThrow StorageException(412, "precondition")
        }
        assertFalse(storeWith(storage).createIfAbsent("claims/RUT/c1", "x".toByteArray()))
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
    fun `overwriteIfGeneration returns true on success and false on 412`() {
        val ok = mock<Storage> {
            on { create(any<BlobInfo>(), any<ByteArray>(), any<Storage.BlobTargetOption>()) } doReturn mock<Blob>()
        }
        assertTrue(storeWith(ok).overwriteIfGeneration("claims/RUT/c1", "x".toByteArray(), 7L))

        val conflict = mock<Storage> {
            on { create(any<BlobInfo>(), any<ByteArray>(), any<Storage.BlobTargetOption>()) } doThrow StorageException(412, "precondition")
        }
        assertFalse(storeWith(conflict).overwriteIfGeneration("claims/RUT/c1", "x".toByteArray(), 7L))
    }
}
