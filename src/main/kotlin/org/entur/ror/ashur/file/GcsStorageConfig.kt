package org.entur.ror.ashur.file

import com.google.cloud.NoCredentials
import com.google.cloud.storage.StorageOptions
import org.rutebanken.helper.gcp.repository.GcsBlobStoreRepository
import org.rutebanken.helper.storage.repository.BlobStoreRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.context.annotation.Scope

@Configuration
@Profile("gcp")
open class GcsStorageConfig {

    private val logger = LoggerFactory.getLogger(GcsStorageConfig::class.java)

    @Bean("mardukBlobStoreRepository")
    @Scope("prototype")
    open fun mardukBlobStoreRepository(
        @Value("\${ashur.gcp.marduk-project-id}") projectId: String?,
        @Value("\${blobstore.gcs.credential.path:#{null}}") credentialPath: String?
    ): BlobStoreRepository {
        return buildBlobStoreRepository(projectId, credentialPath)
    }

    @Bean("ashurBlobStoreRepository")
    @Scope("prototype")
    open fun ashurBlobStoreRepository(
        @Value("\${ashur.gcp.ashur-project-id}") projectId: String?,
        @Value("\${blobstore.gcs.credential.path:#{null}}") credentialPath: String?
    ): BlobStoreRepository {
        return buildBlobStoreRepository(projectId, credentialPath)
    }

    private fun buildBlobStoreRepository(projectId: String?, credentialPath: String?): GcsBlobStoreRepository {
        val emulatorHost = System.getenv("STORAGE_EMULATOR_HOST")
        if (!emulatorHost.isNullOrBlank()) {
            logger.info("Using GCS emulator at {} for project {}", emulatorHost, projectId)
            val storage = StorageOptions.newBuilder()
                .setHost(emulatorHost)
                .setProjectId(projectId)
                .setCredentials(NoCredentials.getInstance())
                .build()
                .service
            return GcsBlobStoreRepository(storage)
        }
        return GcsBlobStoreRepository(projectId, credentialPath)
    }
}
