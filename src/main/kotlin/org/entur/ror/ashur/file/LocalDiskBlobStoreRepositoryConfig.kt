package org.entur.ror.ashur.file

import org.rutebanken.helper.storage.repository.BlobStoreRepository
import org.rutebanken.helper.storage.repository.LocalDiskBlobStoreRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.context.annotation.Scope

@Configuration
@Profile("local", "test")
open class LocalDiskBlobStoreRepositoryConfig {
    @Bean("mardukBlobStoreRepository")
    @Scope("prototype")
    open fun mardukBlobStoreRepository(
        @Value("\${ashur.local.blobstore-path}") baseFolder: String
    ): BlobStoreRepository {
        return LocalDiskBlobStoreRepository(baseFolder)
    }

    @Bean("ashurBlobStoreRepository")
    @Scope("prototype")
    open fun ashurBlobStoreRepository(
        @Value("\${ashur.local.blobstore-path}") baseFolder: String
    ): BlobStoreRepository {
        return LocalDiskBlobStoreRepository(baseFolder)
    }
}
