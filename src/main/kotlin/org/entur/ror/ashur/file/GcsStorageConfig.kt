package org.entur.ror.ashur.file

import com.google.cloud.storage.Storage
import org.rutebanken.helper.gcp.BlobStoreHelper
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
    @Value("\${blobstore.gcs.credential.path:#{null}}")
    private val credentialPath: String? = null

    @Value("\${blobstore.gcs.project.id}")
    private val projectId: String? = null

    @Bean
    open fun storage(): Storage? {
        if (credentialPath == null || credentialPath.isEmpty()) {
            return BlobStoreHelper.getStorage(projectId)
        } else {
            return BlobStoreHelper.getStorage(credentialPath, projectId)
        }
    }

    @Bean
    @Scope("prototype")
    open fun blobStoreRepository(
        @Value("\${blobstore.gcs.project.id}") projectId: String?,
        @Value("\${blobstore.gcs.credential.path:#{null}}") credentialPath: String?
    ): BlobStoreRepository {
        return GcsBlobStoreRepository(projectId, credentialPath)
    }
}
