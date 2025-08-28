package org.entur.ror.ashur.file

import com.google.cloud.storage.Blob
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import org.entur.ror.ashur.gcp.GcsClient

abstract class GcsFileService(
    private val gcsClient: GcsClient,
    private val bucketName: String
): FileService() {

    override fun fileExists(fileName: String): Boolean {
        val blobId = BlobId.of(bucketName, fileName)
        val blob: Blob? = gcsClient.storage[blobId]
        return blob != null && blob.exists()
    }

    override fun getFileAsByteArray(fileName: String): ByteArray {
        val blobId = BlobId.of(bucketName, fileName)
        val blob: Blob? = gcsClient.storage[blobId]
        return blob?.getContent() ?: throw IllegalArgumentException("File not found: $fileName")
    }

    override fun uploadFile(fileName: String, content: ByteArray): Boolean {
        val blobId = BlobId.of(bucketName, fileName)
        val blobInfo = BlobInfo.newBuilder(blobId).build()
        gcsClient.storage.create(blobInfo, content)
        return true
    }
}