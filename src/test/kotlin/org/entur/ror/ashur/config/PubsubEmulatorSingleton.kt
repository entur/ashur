package org.entur.ror.ashur.config

import org.testcontainers.containers.PubSubEmulatorContainer
import org.testcontainers.utility.DockerImageName

object PubSubEmulatorSingleton {

    val container: PubSubEmulatorContainer =
        PubSubEmulatorContainer(
            DockerImageName.parse("gcr.io/google.com/cloudsdktool/cloud-sdk:emulators")
        ).apply { start() }

}