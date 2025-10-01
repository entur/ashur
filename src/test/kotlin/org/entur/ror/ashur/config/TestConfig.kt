package org.entur.ror.ashur.config

import org.entur.ror.ashur.file.AshurBucketService
import org.rutebanken.helper.storage.repository.InMemoryBlobStoreRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
open class TestConfig {

    @Bean
    open fun ashurBucketService(
        appConfig: AppConfig,
        @Qualifier("ashurBlobStoreRepository") repository: InMemoryBlobStoreRepository
    ) = AshurBucketService(
        appConfig = appConfig,
        repository = repository
    )

    @Bean
    open fun mardukBucketService(
        appConfig: AppConfig,
        @Qualifier("mardukBlobStoreRepository") repository: InMemoryBlobStoreRepository
    ) = org.entur.ror.ashur.file.MardukBucketService(
        appConfig = appConfig,
        repository = repository
    )

    @Bean
    @Qualifier("ashurBlobStoreRepository")
    open fun ashurBlobStoreRepository() = InMemoryBlobStoreRepository(emptyMap())

    @Bean
    @Qualifier("mardukBlobStoreRepository")
    open fun mardukBlobStoreRepository() = InMemoryBlobStoreRepository(emptyMap())

}