package org.entur.ror.ashur.file

import com.google.cloud.storage.Blob
import com.google.cloud.storage.BlobId
import org.entur.ror.ashur.gcp.GcsClient
import org.slf4j.LoggerFactory

class GcsFileService(private val gcsClient: GcsClient, private val bucketName: String): FileService() {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun fileExists(fileName: String): Boolean {
        val maxAttempts = 3
        var attempt = 0
        while (attempt < maxAttempts) {
            try {
                val blobId = BlobId.of(bucketName, fileName)
                val blob: Blob? = gcsClient.storage[blobId]
                return blob != null && blob.exists()
            } catch (e: Exception) {
                attempt++
                if (attempt == maxAttempts) throw e
                logger.warn("Exception occurred while checking if file exists in GCS (retrying, attempt ${attempt}/${maxAttempts})", e)
            }
        }
        return false
    }

    override fun getFileAsByteArray(fileName: String): ByteArray {
        val blobId = BlobId.of(bucketName, fileName)
        val blob: Blob? = gcsClient.storage[blobId]
        return blob?.getContent() ?: throw IllegalArgumentException("File not found: $fileName")
    }

    override fun uploadFile(fileName: String, content: ByteArray): Boolean {
        TODO("Not yet implemented")
    }

}