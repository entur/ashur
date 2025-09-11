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
    @Bean
    @Scope("prototype")
    open fun blobStoreRepository(
        @Value("\${ashur.local.blobstorePath:tmp}") baseFolder: String?
    ): BlobStoreRepository {
        return LocalDiskBlobStoreRepository(baseFolder)
    }
}
