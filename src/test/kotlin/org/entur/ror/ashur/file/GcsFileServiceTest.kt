package org.entur.ror.ashur.file

import com.google.cloud.storage.Blob
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import org.entur.ror.ashur.gcp.GcsClient
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class GcsFileServiceTest {
    private val storage = mock(Storage::class.java)
    private val gcsClient: GcsClient = GcsClient(storage)
    private val bucketName = "test-bucket"
    private val fileService = GcsFileService(gcsClient, bucketName)

    private val content = "Hello".toByteArray()
    private val fileName = "test.txt"

    private fun mockBlob(): BlobInfo {
        val blobId = BlobId.of(bucketName, fileName)
        val blobInfo = BlobInfo.newBuilder(blobId).build()
        return blobInfo
    }

    @Test
    fun `uploadFile should return true when upload succeeds`() {
        val blobInfo = mockBlob()

        whenever(gcsClient.storage.create(blobInfo, content)).thenReturn(mock(Blob::class.java))

        val result = fileService.uploadFile(fileName, content)
        assertTrue(result)
        verify(gcsClient.storage).create(blobInfo, content)
    }

    @Test
    fun `uploadFile should retry on failure and return true when upload eventually succeeds`() {
        val blobInfo = mockBlob()

        // Simulate a failure on the first attempt, then success on the second attempt
        whenever(gcsClient.storage.create(blobInfo, content))
            .thenThrow(RuntimeException("Temporary failure"))
            .thenReturn(mock(Blob::class.java))

        val result = fileService.uploadFile(fileName, content)

        assertTrue(result)
        verify(gcsClient.storage, times(2)).create(blobInfo, content)
    }

    @Test
    fun `uploadFile should throw exception if upload has failed after 3 retries`() {
        val blobInfo = mockBlob()

        whenever(gcsClient.storage.create(blobInfo, content))
            .thenThrow(RuntimeException("Temporary failure"))

        assertThrows<RuntimeException> {
            fileService.uploadFile(fileName, content)
        }

        verify(gcsClient.storage, times(3)).create(blobInfo, content)
    }
}