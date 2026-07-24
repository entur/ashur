package org.entur.ror.ashur.config

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.info.BuildProperties
import org.springframework.boot.info.GitProperties
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

/**
 * Logs the running build/git version once, when the application is ready.
 *
 * The [BuildProperties] and [GitProperties] beans are auto-configured by Spring Boot from
 * `META-INF/build-info.properties` and `git.properties`, generated at build time by the
 * spring-boot-maven-plugin `build-info` goal and the git-commit-id-maven-plugin respectively.
 * They are injected via [ObjectProvider] so a run without those files (e.g. an IDE run that
 * skipped the Maven build) logs "unknown" instead of failing to start.
 *
 * [BuildProperties.getTime] is the instant the artifact was built (i.e. when the Docker image's
 * jar was produced in CI); it is rendered in Norwegian local time for readability.
 */
@Component
class VersionLogger(
    private val buildProperties: ObjectProvider<BuildProperties>,
    private val gitProperties: ObjectProvider<GitProperties>,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val buildTimeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Europe/Oslo"))

    @EventListener(ApplicationReadyEvent::class)
    fun logVersion() {
        val build = buildProperties.ifAvailable
        val git = gitProperties.ifAvailable
        val imageBuiltAt = build?.time?.let(buildTimeFormatter::format) ?: "unknown"
        logger.info(
            "Ashur started — commit={}, branch={}, image built {} (Norway time)",
            git?.shortCommitId ?: "unknown",
            git?.branch ?: "unknown",
            imageBuiltAt,
        )
    }
}
