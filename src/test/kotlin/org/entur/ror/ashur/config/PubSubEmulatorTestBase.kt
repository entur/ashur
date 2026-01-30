package org.entur.ror.ashur.config

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PubSubEmulatorContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@Testcontainers
abstract class PubSubEmulatorTestBase {

    companion object {

        @JvmStatic
        @DynamicPropertySource
        fun registerPubSubProperties(registry: DynamicPropertyRegistry) {
            val container = PubSubEmulatorSingleton.container

            registry.add("spring.cloud.gcp.pubsub.emulator-host") {
                container.emulatorEndpoint
            }
            registry.add("camel.component.google-pubsub.endpoint") {
                container.emulatorEndpoint
            }
        }
    }
}