package org.entur.ror.ashur.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "camel.component")
class CamelConfig(
    var googlePubsub: PubsubConfig = PubsubConfig(),
) {
    class PubsubConfig {
        lateinit var endpoint: String
    }
}