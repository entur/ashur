package org.entur.ror.ashur.file

import com.google.cloud.storage.Storage
import org.entur.ror.ashur.config.AppConfig
import org.entur.ror.ashur.gcp.GcsClient
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

class MardukBucketServiceTest {
    private val storage = mock(Storage::class.java)
    private val gcsClient: GcsClient = GcsClient(storage)
    private val appConfig = AppConfig(
        gcp = AppConfig.GcpConfig().also {
            it.mardukBucketName = "test-marduk-bucket-name"
        }
    )

    @Test
    fun testMardukBucketService() {
        val service = MardukBucketService(
            gcsClient = gcsClient,
            appConfig = appConfig,
        )
        assert(service.bucketName == "test-marduk-bucket-name")
    }
}