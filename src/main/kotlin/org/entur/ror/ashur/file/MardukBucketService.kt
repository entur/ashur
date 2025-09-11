package org.entur.ror.ashur.file

import org.entur.ror.ashur.config.AppConfig
import org.rutebanken.helper.storage.repository.BlobStoreRepository
import org.springframework.stereotype.Service

@Service
class MardukBucketService(
    appConfig: AppConfig,
    repository: BlobStoreRepository
) : AbstractBlobStoreService(
    containerName = appConfig.gcp.mardukBucketName,
    repository = repository
)
