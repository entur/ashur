package org.entur.ror.ashur.file

import org.entur.ror.ashur.config.AppConfig
import org.entur.ror.ashur.gcp.GcsClient
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component("mardukBucketService")
@Profile("gcp")
class MardukBucketService(
    gcsClient: GcsClient,
    appConfig: AppConfig
): GcsFileService(
    gcsClient = gcsClient,
    bucketName = appConfig.gcp.mardukBucketName,
)
