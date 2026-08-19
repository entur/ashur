package org.entur.ror.ashur.file

import com.google.cloud.NoCredentials
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

/**
 * Raw [Storage] client for the redelivery guard's claim objects. Separate from
 * [GcsStorageConfig]'s [org.rutebanken.helper.storage.repository.BlobStoreRepository] because the
 * guard needs generation preconditions the repository abstraction does not expose.
 */
@Configuration
@Profile("gcp")
open class GcsClaimStorageConfig {

    @Bean("claimStorage")
    open fun claimStorage(
        @Value("\${ashur.gcp.ashur-project-id}") projectId: String?,
    ): Storage {
        val emulatorHost = System.getenv("STORAGE_EMULATOR_HOST")
        if (!emulatorHost.isNullOrBlank()) {
            return StorageOptions.newBuilder()
                .setHost(emulatorHost)
                .setProjectId(projectId)
                .setCredentials(NoCredentials.getInstance())
                .build()
                .service
        }
        return StorageOptions.newBuilder()
            .setProjectId(projectId)
            .build()
            .service
    }
}
