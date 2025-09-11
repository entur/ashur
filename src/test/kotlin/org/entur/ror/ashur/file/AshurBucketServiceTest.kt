package org.entur.ror.ashur.file

import org.entur.ror.ashur.config.AppConfig
import org.junit.jupiter.api.Test
import org.rutebanken.helper.storage.repository.LocalDiskBlobStoreRepository

class AshurBucketServiceTest {
    private val appConfig = AppConfig(
        gcp = AppConfig.GcpConfig().also {
            it.ashurBucketName = "test-ashur-bucket-name"
        }
    )

    @Test
    fun testAshurBucketService() {
        val service = AshurBucketService(
            appConfig = appConfig,
            repository = LocalDiskBlobStoreRepository("tmp")
        )
        assert(service.containerName == "test-ashur-bucket-name")
    }
}