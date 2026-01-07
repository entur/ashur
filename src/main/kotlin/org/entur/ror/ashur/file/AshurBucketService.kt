package org.entur.ror.ashur.file

import org.entur.ror.ashur.config.AppConfig
import org.rutebanken.helper.storage.repository.BlobStoreRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class AshurBucketService(
    val appConfig: AppConfig,
    @Qualifier("ashurBlobStoreRepository") repository: BlobStoreRepository
) : AbstractBlobStoreService(
    containerName = appConfig.gcp.ashurBucketName,
    repository = repository
) {
    fun copyToAshurExchangeBucket(sourceObjectName: String, destination: String) {
        copyBlob(
            sourceObjectName = sourceObjectName,
            targetContainerName = appConfig.gcp.ashurExchangeBucketName,
            targetObjectNam = destination,
        )
    }
}
