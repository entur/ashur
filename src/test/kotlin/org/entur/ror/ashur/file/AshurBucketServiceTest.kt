package org.entur.ror.ashur.file

import com.google.cloud.storage.Storage
import org.entur.ror.ashur.config.AppConfig
import org.entur.ror.ashur.gcp.GcsClient
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

class AshurBucketServiceTest {
    private val storage = mock(Storage::class.java)
    private val gcsClient: GcsClient = GcsClient(storage)
    private val appConfig = AppConfig(
        gcp = AppConfig.GcpConfig().also {
            it.ashurBucketName = "test-ashur-bucket-name"
        }
    )

    @Test
    fun testAshurBucketService() {
        val service = AshurBucketService(
            gcsClient = gcsClient,
            appConfig = appConfig,
        )
        assert(service.bucketName == "test-ashur-bucket-name")
    }
}