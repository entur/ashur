package org.entur.ror.ashur.file

import org.entur.ror.ashur.config.AppConfig
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component("mardukBucketService")
@Profile("local")
class LocalMardukBucketService(appConfig: AppConfig): LocalFileService(
    bucketPath = appConfig.local.mardukBucketPath,
)