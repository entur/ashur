package org.entur.ror.ashur.file

import com.google.cloud.storage.Blob
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import org.entur.ror.ashur.config.AppConfig
import org.entur.ror.ashur.gcp.GcsClient
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("gcp")
@Component
class GcsFileService(
    private val gcsClient: GcsClient,
    appConfig: AppConfig
): FileService() {
    private val bucketName = appConfig.gcp.bucketName

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