package org.entur.ror.ashur.file

import org.entur.ror.ashur.config.AppConfig
import org.junit.jupiter.api.Test
import org.rutebanken.helper.storage.repository.LocalDiskBlobStoreRepository

class MardukBucketServiceTest {
    private val appConfig = AppConfig(
        gcp = AppConfig.GcpConfig().also {
            it.mardukBucketName = "test-marduk-bucket-name"
        }
    )

    @Test
    fun testMardukBucketService() {
        val service = MardukBucketService(
            appConfig = appConfig,
            repository = LocalDiskBlobStoreRepository("tmp")
        )
        assert(service.containerName == "test-marduk-bucket-name")
    }
}