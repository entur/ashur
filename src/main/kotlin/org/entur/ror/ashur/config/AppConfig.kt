package org.entur.ror.ashur.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "ashur")
class AppConfig(
    var netex: NetexConfig = NetexConfig(),
    var gcp: GcpConfig = GcpConfig(),
    var local: LocalConfig = LocalConfig(),
    var redeliveryGuard: RedeliveryGuardConfig = RedeliveryGuardConfig(),
) {
    class NetexConfig {
        lateinit var inputPath: String
        lateinit var outputPath: String
        var cleanupEnabled: Boolean = false
    }

    class GcpConfig {
        lateinit var ashurProjectId: String
        lateinit var ashurBucketName: String
        lateinit var ashurExchangeBucketName: String
        lateinit var mardukProjectId: String
        lateinit var mardukBucketName: String
    }

    class LocalConfig {
        lateinit var blobstorePath: String
    }

    class RedeliveryGuardConfig {
        /** When false the guard is a complete no-op (every delivery processes as today). */
        var enabled: Boolean = true

        /** A claim older than this is considered stale (holder presumed dead) and may be taken over. */
        var ttlSeconds: Long = 1200
    }
}
