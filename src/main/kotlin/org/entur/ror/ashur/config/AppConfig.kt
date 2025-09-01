package org.entur.ror.ashur.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "ashur")
class AppConfig(
    var netex: NetexConfig = NetexConfig(),
    var gcp: GcpConfig = GcpConfig(),
    var local: LocalConfig = LocalConfig(),
) {
    class NetexConfig {
        lateinit var inputPath: String
        lateinit var outputPath: String
        var cleanupEnabled: Boolean = false
    }

    class GcpConfig {
        lateinit var ashurProjectId: String
        lateinit var ashurBucketName: String
        lateinit var mardukProjectId: String
        lateinit var mardukBucketName: String
    }

    class LocalConfig {
        lateinit var ashurBucketPath: String
        lateinit var mardukBucketPath: String
    }
}
