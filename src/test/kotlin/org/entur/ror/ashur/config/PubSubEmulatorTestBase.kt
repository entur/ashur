package org.entur.ror.ashur.config

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PubSubEmulatorContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.utility.DockerImageName

abstract class PubSubEmulatorTestBase {
    companion object {
        @Container
        val pubsubEmulator = PubSubEmulatorContainer(
            DockerImageName.parse("gcr.io/google.com/cloudsdktool/cloud-sdk:emulators")
        )

        @JvmStatic
        @DynamicPropertySource
        fun registerPubSubProperties(registry: DynamicPropertyRegistry) {
            pubsubEmulator.start()
            registry.add(
                "spring.cloud.gcp.pubsub.emulator-host",
                { pubsubEmulator.emulatorEndpoint }
            )
            registry.add(
                "camel.component.google-pubsub.endpoint",
                { pubsubEmulator.emulatorEndpoint }
            )
        }
    }
}