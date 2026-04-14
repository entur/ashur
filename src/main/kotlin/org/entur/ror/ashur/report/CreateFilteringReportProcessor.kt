package org.entur.ror.ashur.report

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.entur.netex.tools.lib.report.FilterReport
import org.entur.ror.ashur.Constants
import org.entur.ror.ashur.file.AshurBucketService
import org.entur.ror.ashur.getCodespace
import org.entur.ror.ashur.getCorrelationId
import org.entur.ror.ashur.getReason
import org.entur.ror.ashur.getStatus
import org.entur.ror.ashur.toPubsubMessage
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class CreateFilteringReportProcessor(
    private val ashurBucketService: AshurBucketService,
) : Processor {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val mapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    fun toJson(report: FilteringReport): String {
        return mapper.writeValueAsString(report)
    }

    fun aggregateEntityTypeCounts(filterReport: FilterReport): Map<String, Int> {
        val counts = mutableMapOf<String, Int>()
        for ((_, typeCounts) in filterReport.elementTypesByFile) {
            for ((type, count) in typeCounts) {
                counts[type] = (counts[type] ?: 0) + count
            }
        }
        return counts
    }

    override fun process(exchange: Exchange) {
        val pubsubMessage = exchange.toPubsubMessage()
        val filterReport = exchange.getIn().getHeader(Constants.FILTER_REPORT_HEADER, FilterReport::class.java)
        exchange.getIn().removeHeader(Constants.FILTER_REPORT_HEADER)
        val filterProfile = exchange.getIn().getHeader(Constants.FILTERING_PROFILE_HEADER, String::class.java)
        val report = FilteringReport(
            correlationId = pubsubMessage.getCorrelationId() ?: "unknown",
            codespace = pubsubMessage.getCodespace() ?: "unknown",
            filterProfile = filterProfile,
            status = pubsubMessage.getStatus() ?: "unknown",
            reason = pubsubMessage.getReason(),
            entityTypeCounts = filterReport?.let { aggregateEntityTypeCounts(it) },
            created = LocalDateTime.now(),
        )

        val reportJson = toJson(report)

        val filteringReportPath = "reports/${report.codespace}/filtering-report-${report.correlationId}.json"
        logger.info("Uploading filtering report to path $filteringReportPath")
        ashurBucketService.uploadBlob(
            filteringReportPath,
            reportJson.byteInputStream()
        )
        logger.info("Uploaded filtering report successfully")

        exchange.getIn().body = reportJson
    }
}