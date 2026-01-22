package org.entur.ror.ashur.report

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.support.DefaultExchange
import org.apache.camel.test.spring.junit5.CamelSpringBootTest
import org.entur.ror.ashur.AshurApplication
import org.entur.ror.ashur.Constants
import org.entur.ror.ashur.config.AppConfig
import org.entur.ror.ashur.config.PubSubEmulatorTestBase
import org.entur.ror.ashur.file.AshurBucketService
import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.junit.jupiter.Testcontainers
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@Testcontainers
@CamelSpringBootTest
@SpringBootTest(classes = [AshurApplication::class])
class CreateFilteringReportProcessorTest: PubSubEmulatorTestBase() {
    @Autowired
    lateinit var appConfig: AppConfig

    @Autowired
    lateinit var ashurBucketService: AshurBucketService

    private val testCodespace = "test-codespace"

    fun pathToFilteringReport(correlationId: String) = "reports/${testCodespace}/filtering-report-${correlationId}.json"

    fun fileExistsInAshurInternalBucket(filePath: String): Boolean {
        val target = File("${appConfig.local.blobstorePath}/${appConfig.gcp.ashurBucketName}/$filePath")
        return target.exists()
    }

    fun getReport(filePath: String): FilteringReport {
        val target = File("${appConfig.local.blobstorePath}/${appConfig.gcp.ashurBucketName}/$filePath")
        val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())
        return objectMapper.readValue(target, FilteringReport::class.java)
    }

    @Test
    fun testFilteringReportCreationOnSuccess() {
        val exchange = DefaultExchange(DefaultCamelContext())

        val succeedingCorrelationId = "succeeding-correlation-id"
        val reportPath = pathToFilteringReport(succeedingCorrelationId)

        exchange.getIn().setHeader("CamelGooglePubsubAttributes", mapOf(
            Constants.CORRELATION_ID_HEADER to succeedingCorrelationId,
            Constants.CODESPACE_HEADER to testCodespace,
            Constants.FILTERING_REPORT_STATUS_HEADER to Constants.FILTER_NETEX_FILE_STATUS_SUCCEEDED
        ))
        CreateFilteringReportProcessor(ashurBucketService).process(exchange)

        val reportFileExists = fileExistsInAshurInternalBucket(reportPath)
        assertTrue(reportFileExists)

        if (reportFileExists) {
            val report = getReport(reportPath)
            assertNotNull(report.created)
            assertEquals(succeedingCorrelationId, report.correlationId)
            assertEquals(testCodespace, report.codespace)
            assertEquals(Constants.FILTER_NETEX_FILE_STATUS_SUCCEEDED, report.status)
            assertNull(report.reason)
        }

        cleanupTestZipFiles()
    }

    @Test
    fun testFilteringReportCreationOnFailure() {
        val exchange = DefaultExchange(DefaultCamelContext())

        val failingCorrelationId = "failing-correlation-id"
        val reasonForFailure = "Some failure reason"
        val reportPath = pathToFilteringReport(failingCorrelationId)

        exchange.getIn().setHeader("CamelGooglePubsubAttributes", mapOf(
            Constants.CORRELATION_ID_HEADER to failingCorrelationId,
            Constants.CODESPACE_HEADER to testCodespace,
            Constants.FILTERING_REPORT_STATUS_HEADER to Constants.FILTER_NETEX_FILE_STATUS_FAILED,
            Constants.FILTERING_FAILURE_REASON_HEADER to reasonForFailure
        ))
        CreateFilteringReportProcessor(ashurBucketService).process(exchange)

        val reportFileExists = fileExistsInAshurInternalBucket(reportPath)
        assertTrue(reportFileExists)

        if (reportFileExists) {
            val report = getReport(reportPath)
            assertNotNull(report.created)
            assertEquals(failingCorrelationId, report.correlationId)
            assertEquals(testCodespace, report.codespace)
            assertEquals(Constants.FILTER_NETEX_FILE_STATUS_FAILED, report.status)
            assertEquals(reasonForFailure, report.reason)
        }

        cleanupTestZipFiles()
    }

    fun cleanupTestZipFiles() {
        val target = File("${appConfig.local.blobstorePath}/${appConfig.gcp.mardukBucketName}")
        if (target.exists()) {
            target.deleteRecursively()
        }
    }
}