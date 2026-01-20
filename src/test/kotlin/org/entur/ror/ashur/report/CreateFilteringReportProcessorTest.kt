package org.entur.ror.ashur.report

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.support.DefaultExchange
import org.apache.camel.test.spring.junit5.CamelSpringBootTest
import org.entur.ror.ashur.AshurApplication
import org.entur.ror.ashur.Constants
import org.entur.ror.ashur.config.AppConfig
import org.entur.ror.ashur.config.PubSubEmulatorTestBase
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

    private val testCorrelationId = "test-correlation-id"
    private val testCodespace = "test-codespace"

    fun pathToFilteringReport() = "reports/${testCodespace}/filtering-report-${testCorrelationId}.json"

    fun fileExistsInAshurInternalBucket(filePath: String): Boolean {
        val target = File("${appConfig.local.blobstorePath}/${appConfig.gcp.ashurBucketName}/$filePath")
        return target.exists()
    }

    fun getReport(filePath: String): FilteringReport {
        val target = File("${appConfig.local.blobstorePath}/${appConfig.gcp.ashurBucketName}/$filePath")
        val objectMapper = ObjectMapper()
        return objectMapper.readValue(target, FilteringReport::class.java)
    }

    @Test
    fun testFilteringReportCreationOnSuccess() {
        val exchange = DefaultExchange(DefaultCamelContext())
        exchange.getIn().setHeader("CamelGooglePubsubAttributes", mapOf(
            Constants.CORRELATION_ID_HEADER to testCorrelationId,
            Constants.FILTERING_REPORT_STATUS_HEADER to Constants.FILTER_NETEX_FILE_STATUS_SUCCEEDED
        ))
        CreateFilteringReportProcessor().process(exchange)

        val reportFileExists = fileExistsInAshurInternalBucket(pathToFilteringReport())
        assertTrue(reportFileExists)

        if (reportFileExists) {
            val report = getReport(pathToFilteringReport())
            assertNotNull(report.created)
            assertEquals(testCorrelationId, report.correlationId)
            assertEquals(testCodespace, report.codespace)
            assertEquals(Constants.FILTER_NETEX_FILE_STATUS_SUCCEEDED, report.status)
            assertNull(report.reason)
        }
    }

    @Test
    fun testFilteringReportCreationOnFailure() {
        val exchange = DefaultExchange(DefaultCamelContext())
        val reasonForFailure = "Some failure reason"
        exchange.getIn().setHeader("CamelGooglePubsubAttributes", mapOf(
            Constants.CORRELATION_ID_HEADER to testCorrelationId,
            Constants.FILTERING_REPORT_STATUS_HEADER to Constants.FILTER_NETEX_FILE_STATUS_FAILED,
            Constants.FILTERING_FAILURE_REASON_HEADER to reasonForFailure
        ))
        CreateFilteringReportProcessor().process(exchange)

        val reportFileExists = fileExistsInAshurInternalBucket(pathToFilteringReport())
        assertTrue(reportFileExists)

        if (reportFileExists) {
            val report = getReport(pathToFilteringReport())
            assertNotNull(report.created)
            assertEquals(testCorrelationId, report.correlationId)
            assertEquals(testCodespace, report.codespace)
            assertEquals(Constants.FILTER_NETEX_FILE_STATUS_FAILED, report.status)
            assertEquals(reasonForFailure, report.reason)
        }
    }
}