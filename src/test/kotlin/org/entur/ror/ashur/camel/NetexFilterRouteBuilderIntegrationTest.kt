package org.entur.ror.ashur.camel

import org.apache.camel.CamelContext
import org.apache.camel.ConsumerTemplate
import org.apache.camel.ProducerTemplate
import org.apache.camel.test.spring.junit5.CamelSpringBootTest
import org.entur.ror.ashur.AshurApplication
import org.entur.ror.ashur.Constants
import org.entur.ror.ashur.config.AppConfig
import org.entur.ror.ashur.config.PubSubEmulatorTestBase
import org.entur.ror.ashur.getCorrelationId
import org.entur.ror.ashur.getPathOfFilteredFile
import org.entur.ror.ashur.getStatus
import org.entur.ror.ashur.toPubsubMessage
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.junit.jupiter.Testcontainers
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.inputStream
import kotlin.test.assertEquals

@Testcontainers
@CamelSpringBootTest
@SpringBootTest(classes = [AshurApplication::class])
class NetexFilterRouteBuilderIntegrationTest: PubSubEmulatorTestBase() {
    @Autowired
    lateinit var appConfig: AppConfig

    @Autowired
    lateinit var producerTemplate: ProducerTemplate

    @Autowired
    lateinit var consumerTemplate: ConsumerTemplate

    @Autowired
    lateinit var context: CamelContext

    private val testCodespace = "test-codespace"
    private val testSource = "test-source"
    private val testFilteringProfile = "AsIsImportFilter"

    fun sendFilterMessageToPubsub(
        netexFilePath: String,
        correlationId: String,
    ) {
        val ashurProjectId = appConfig.gcp.ashurProjectId
        producerTemplate.requestBodyAndHeader(
            "google-pubsub:$ashurProjectId:${Constants.FILTER_NETEX_FILE_SUBSCRIPTION}",
            "",
            "CamelGooglePubsubAttributes",
            mapOf(
                Constants.CODESPACE_HEADER to testCodespace,
                Constants.CORRELATION_ID_HEADER to correlationId,
                Constants.FILTERING_PROFILE_HEADER to testFilteringProfile,
                Constants.NETEX_FILE_NAME_HEADER to netexFilePath,
                Constants.NETEX_SOURCE_HEADER to testSource,
            )
        )
    }

    fun pathOfFilteredFile(fileName: String, correlationId: String) = "${testCodespace}/${correlationId}/${testSource}/filtered_${fileName}"
    fun pathOfFilteringReport(correlationId: String) = "reports/${testCodespace}/filtering-report-${correlationId}.json"

    fun fileExistsInAshurInternalBucket(filePath: String): Boolean {
        val target = File("${appConfig.local.blobstorePath}/${appConfig.gcp.ashurBucketName}/$filePath")
        return target.exists()
    }

    fun copyTestZipFileToMardukTestBucket() {
        val resource = Paths.get("src/test/resources/testfile.zip").inputStream()
        val target = File("${appConfig.local.blobstorePath}/${appConfig.gcp.mardukBucketName}")
        if (!target.exists()) {
            target.mkdirs()
        }
        val targetFile = File(target, "testfile.zip")
        if (!targetFile.exists()) {
            targetFile.createNewFile()
        }
        resource.use { input ->
            targetFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    fun cleanupTestZipFiles() {
        val target = File("${appConfig.local.blobstorePath}/${appConfig.gcp.mardukBucketName}")
        if (target.exists()) {
            target.deleteRecursively()
        }
    }

    @Test
    fun `test filter route processes message successfully`() {
        copyTestZipFileToMardukTestBucket()
        val mardukProjectId = appConfig.gcp.mardukProjectId
        val correlationId = "success-correlation-id"

        sendFilterMessageToPubsub(
            netexFilePath = "testfile.zip",
            correlationId = correlationId,
        )
        val startedMessage = consumerTemplate.receive(
            "google-pubsub:$mardukProjectId:${Constants.FILTER_NETEX_FILE_STATUS_TOPIC}?synchronousPull=true",
            5000
        )

        val successMessage = consumerTemplate.receive(
            "google-pubsub:$mardukProjectId:${Constants.FILTER_NETEX_FILE_STATUS_TOPIC}?synchronousPull=true",
            10000
        )

        assertEquals(correlationId, startedMessage.toPubsubMessage().getCorrelationId())
        assertEquals(correlationId, successMessage.toPubsubMessage().getCorrelationId())

        assertEquals(Constants.FILTER_NETEX_FILE_STATUS_STARTED, startedMessage.toPubsubMessage().getStatus())
        assertEquals(Constants.FILTER_NETEX_FILE_STATUS_SUCCEEDED, successMessage.toPubsubMessage().getStatus())

        val pathOfFilteredFile = pathOfFilteredFile("testfile.zip", correlationId)
        assertEquals(pathOfFilteredFile, successMessage.toPubsubMessage().getPathOfFilteredFile())

        val expectedFilteringReportPath = pathOfFilteringReport(correlationId)
        assertTrue(fileExistsInAshurInternalBucket(expectedFilteringReportPath))

        cleanupTestZipFiles()
    }

    @Test
    fun `test filter route processes message but fails because file does not exist`() {
        val mardukProjectId = appConfig.gcp.mardukProjectId
        val failingCorrelationId = "failing-correlation-id"

        sendFilterMessageToPubsub(netexFilePath = "unknown-file.zip", correlationId = failingCorrelationId)

        val startedMessage = consumerTemplate.receive(
            "google-pubsub:$mardukProjectId:${Constants.FILTER_NETEX_FILE_STATUS_TOPIC}?synchronousPull=true",
            5000
        )

        val failedMessage = consumerTemplate.receive(
            "google-pubsub:$mardukProjectId:${Constants.FILTER_NETEX_FILE_STATUS_TOPIC}?synchronousPull=true",
            10000
        )

        assertEquals(failingCorrelationId, startedMessage.toPubsubMessage().getCorrelationId())
        assertEquals(failingCorrelationId, failedMessage.toPubsubMessage().getCorrelationId())

        val expectedFilteringReportPath = pathOfFilteringReport(failingCorrelationId)
        assertTrue(fileExistsInAshurInternalBucket(expectedFilteringReportPath))

        assertEquals(Constants.FILTER_NETEX_FILE_STATUS_STARTED, startedMessage.toPubsubMessage().getStatus())
        assertEquals(Constants.FILTER_NETEX_FILE_STATUS_FAILED, failedMessage.toPubsubMessage().getStatus())

        cleanupTestZipFiles()
    }
}
