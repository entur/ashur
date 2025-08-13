package org.entur.ror.ashur.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "ashur")
class AppConfig(
    var pubsub: PubsubConfig = PubsubConfig(),
    var netex: NetexConfig = NetexConfig(),
    var gcp: GcpConfig = GcpConfig(),
) {
    class PubsubConfig {
        lateinit var projectId: String
        lateinit var subscription: String
    }

    class NetexConfig {
        lateinit var inputPath: String
        lateinit var outputPath: String
        var cleanupEnabled: Boolean = false
    }

    class GcpConfig {
        lateinit var bucketName: String
    }
}
