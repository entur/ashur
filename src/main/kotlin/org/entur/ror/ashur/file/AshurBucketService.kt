package org.entur.ror.ashur.file

import org.entur.ror.ashur.config.AppConfig
import org.rutebanken.helper.storage.repository.BlobStoreRepository
import org.springframework.stereotype.Service

@Service
class AshurBucketService(
    appConfig: AppConfig,
    repository: BlobStoreRepository
) : AbstractBlobStoreService(
    containerName = appConfig.gcp.ashurBucketName,
    repository = repository
)
