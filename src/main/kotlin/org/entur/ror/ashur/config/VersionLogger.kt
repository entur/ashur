package org.entur.ror.ashur.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.info.GitProperties
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

/**
 * Logs the running git version once, when the application is ready.
 *
 * The [GitProperties] bean is auto-configured by Spring Boot from `git.properties`, generated at
 * build time by the git-commit-id-maven-plugin.
 *
 * Restricted to the `gcp` profile (the deployed dev/tst/prd environments), where the metadata
 * reflects the CI-built jar that is actually running.
 */
@Profile("gcp")
@Component
class VersionLogger(private val gitProperties: ObjectProvider<GitProperties>) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @EventListener(ApplicationReadyEvent::class)
    fun logVersion() {
        val git = gitProperties.ifAvailable
        logger.info(
            "Ashur started: commit={}, branch={}",
            git?.shortCommitId ?: "unknown",
            git?.branch ?: "unknown",
        )
    }
}
