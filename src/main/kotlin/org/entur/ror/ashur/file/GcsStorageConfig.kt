package org.entur.ror.ashur.file

import org.rutebanken.helper.gcp.repository.GcsBlobStoreRepository
import org.rutebanken.helper.storage.repository.BlobStoreRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.context.annotation.Scope

@Configuration
@Profile("gcs")
open class GcsStorageConfig {
    @Bean("mardukBlobStoreRepository")
    @Scope("prototype")
    open fun mardukBlobStoreRepository(
        @Value("\${marduk.gcp.marduk-project-id}") projectId: String?,
        @Value("\${blobstore.gcs.credential.path:#{null}}") credentialPath: String?
    ): BlobStoreRepository {
        return GcsBlobStoreRepository(projectId, credentialPath)
    }

    @Bean("ashurBlobStoreRepository")
    @Scope("prototype")
    open fun ashurBlobStoreRepository(
        @Value("\${ashur.gcp.marduk-project-id}") projectId: String?,
        @Value("\${blobstore.gcs.credential.path:#{null}}") credentialPath: String?
    ): BlobStoreRepository {
        return GcsBlobStoreRepository(projectId, credentialPath)
    }
}
