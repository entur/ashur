package org.entur.ror.ashur.config

import org.entur.ror.ashur.file.LocalAshurBucketService
import org.entur.ror.ashur.file.LocalMardukBucketService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class TestBeansConfig {
    @Bean("ashurBucketService")
    open fun ashurBucketService(appConfig: AppConfig) = LocalAshurBucketService(appConfig)

    @Bean("mardukBucketService")
    open fun mardukBucketService(appConfig: AppConfig) = LocalMardukBucketService(appConfig)
}