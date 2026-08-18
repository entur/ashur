package org.entur.ror.ashur.camel

import io.micrometer.core.instrument.MeterRegistry
import org.apache.camel.CamelContext
import org.apache.camel.ConsumerTemplate
import org.apache.camel.Exchange
import org.apache.camel.ProducerTemplate
import org.apache.camel.test.spring.junit5.CamelSpringBootTest
import tools.jackson.module.kotlin.jacksonObjectMapper
import org.entur.ror.ashur.AshurApplication
import org.entur.ror.ashur.Constants
import org.entur.ror.ashur.config.AppConfig
import org.entur.ror.ashur.config.PubSubEmulatorTestBase
import org.entur.ror.ashur.metrics.FilterMetrics
import org.entur.ror.ashur.getCorrelationId
import org.entur.ror.ashur.getFilterProfile
import org.entur.ror.ashur.getPathOfFilteredFile
import org.entur.ror.ashur.getStatus
import org.entur.ror.ashur.toPubsubMessage
import org.entur.ror.ashur.filter.FilterProfile
import org.entur.ror.ashur.report.FilteringReport
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.junit.jupiter.Testcontainers
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.inputStream
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

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

    @Autowired
    lateinit var meterRegistry: MeterRegistry

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

    private val mapper = jacksonObjectMapper()

    fun runCount(status: String): Double =
        meterRegistry.find(FilterMetrics.RUNS_METRIC_NAME)
            .tags("status", status, "codespace", testCodespace)
            .counter()
            ?.count() ?: 0.0

    fun durationRunCount(): Long =
        meterRegistry.find(FilterMetrics.DURATION_METRIC_NAME)
            .tags("codespace", testCodespace)
            .timer()
            ?.count() ?: 0L

    fun pathOfFilteredFile(fileName: String, correlationId: String) =
        "${testCodespace}/${correlationId}/${testFilteringProfile}/filtered_${fileName}"
    fun pathOfFilteringReport(correlationId: String) = "reports/${testCodespace}/filtering-report-${correlationId}.json"

    private fun receiveStatusMessage(timeoutMillis: Long): Exchange {
        val uri = "google-pubsub:${appConfig.gcp.mardukProjectId}:${Constants.FILTER_NETEX_FILE_STATUS_TOPIC}?synchronousPull=true"
        return assertNotNull(
            consumerTemplate.receive(uri, timeoutMillis),
            "no status message on ${Constants.FILTER_NETEX_FILE_STATUS_TOPIC} within ${timeoutMillis}ms",
        )
    }

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
        val correlationId = "success-correlation-id"
        val successRunsBefore = runCount(FilterMetrics.STATUS_SUCCESS)
        val durationRunsBefore = durationRunCount()

        sendFilterMessageToPubsub(
            netexFilePath = "testfile.zip",
            correlationId = correlationId,
        )
        val startedMessage = receiveStatusMessage(5000)

        val successMessage = receiveStatusMessage(10000)

        assertEquals(correlationId, startedMessage.toPubsubMessage().getCorrelationId())
        assertEquals(correlationId, successMessage.toPubsubMessage().getCorrelationId())

        assertEquals(Constants.FILTER_NETEX_FILE_STATUS_STARTED, startedMessage.toPubsubMessage().getStatus())
        assertEquals(Constants.FILTER_NETEX_FILE_STATUS_SUCCEEDED, successMessage.toPubsubMessage().getStatus())

        assertEquals(FilterProfile.AsIsImportFilter, startedMessage.toPubsubMessage().getFilterProfile())
        assertEquals(FilterProfile.AsIsImportFilter, successMessage.toPubsubMessage().getFilterProfile())

        val pathOfFilteredFile = pathOfFilteredFile("testfile.zip", correlationId)
        assertEquals(pathOfFilteredFile, successMessage.toPubsubMessage().getPathOfFilteredFile())
        assertTrue(fileExistsInAshurInternalBucket(pathOfFilteredFile))

        val successBody = successMessage.toPubsubMessage().data.toStringUtf8()
        val successReport = mapper.readValue(successBody, FilteringReport::class.java)
        assertEquals(correlationId, successReport.correlationId)
        assertEquals(testCodespace, successReport.codespace)
        assertEquals(testFilteringProfile, successReport.filterProfile)
        assertEquals(Constants.FILTER_NETEX_FILE_STATUS_SUCCEEDED, successReport.status)
        assertNotNull(successReport.entityTypeCounts)

        val expectedFilteringReportPath = pathOfFilteringReport(correlationId)
        assertTrue(fileExistsInAshurInternalBucket(expectedFilteringReportPath))

        assertEquals(successRunsBefore + 1.0, runCount(FilterMetrics.STATUS_SUCCESS))
        assertEquals(durationRunsBefore + 1, durationRunCount())

        cleanupTestZipFiles()
    }

    @Test
    fun `test filter route processes message but fails because file does not exist`() {
        val failingCorrelationId = "failing-correlation-id"
        val failedRunsBefore = runCount(FilterMetrics.STATUS_FAILED)
        val durationRunsBefore = durationRunCount()

        sendFilterMessageToPubsub(netexFilePath = "unknown-file.zip", correlationId = failingCorrelationId)

        val startedMessage = receiveStatusMessage(5000)

        val failedMessage = receiveStatusMessage(10000)

        assertEquals(failingCorrelationId, startedMessage.toPubsubMessage().getCorrelationId())
        assertEquals(failingCorrelationId, failedMessage.toPubsubMessage().getCorrelationId())

        val expectedFilteringReportPath = pathOfFilteringReport(failingCorrelationId)
        assertTrue(fileExistsInAshurInternalBucket(expectedFilteringReportPath))

        assertEquals(Constants.FILTER_NETEX_FILE_STATUS_STARTED, startedMessage.toPubsubMessage().getStatus())
        assertEquals(Constants.FILTER_NETEX_FILE_STATUS_FAILED, failedMessage.toPubsubMessage().getStatus())

        assertEquals(FilterProfile.AsIsImportFilter, startedMessage.toPubsubMessage().getFilterProfile())
        assertEquals(FilterProfile.AsIsImportFilter, failedMessage.toPubsubMessage().getFilterProfile())

        val failedBody = failedMessage.toPubsubMessage().data.toStringUtf8()
        val failedReport = mapper.readValue(failedBody, FilteringReport::class.java)
        assertEquals(failingCorrelationId, failedReport.correlationId)
        assertEquals(testCodespace, failedReport.codespace)
        assertEquals(testFilteringProfile, failedReport.filterProfile)
        assertEquals(Constants.FILTER_NETEX_FILE_STATUS_FAILED, failedReport.status)

        assertEquals(failedRunsBefore + 1.0, runCount(FilterMetrics.STATUS_FAILED))
        assertEquals(durationRunsBefore, durationRunCount())

        cleanupTestZipFiles()
    }
}
