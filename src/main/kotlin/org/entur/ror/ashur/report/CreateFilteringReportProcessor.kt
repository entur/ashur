package org.entur.ror.ashur.report

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.entur.ror.ashur.file.AshurBucketService
import org.entur.ror.ashur.getCodespace
import org.entur.ror.ashur.getCorrelationId
import org.entur.ror.ashur.getReason
import org.entur.ror.ashur.getStatus
import org.entur.ror.ashur.toPubsubMessage
import org.springframework.stereotype.Component
import java.io.InputStream
import java.time.LocalDateTime

@Component
class CreateFilteringReportProcessor(
    private val ashurBucketService: AshurBucketService,
): Processor {
    fun toJsonInputStream(report: FilteringReport): InputStream {
        val mapper = jacksonObjectMapper().registerModule(JavaTimeModule())
        val json = mapper.writeValueAsString(report)
        return json.byteInputStream()
    }

    override fun process(exchange: Exchange) {
        val pubsubMessage = exchange.toPubsubMessage()
        val report = FilteringReport(
            correlationId = pubsubMessage.getCorrelationId() ?: "unknown",
            codespace = pubsubMessage.getCodespace() ?: "unknown",
            status = pubsubMessage.getStatus() ?: "unknown",
            reason = pubsubMessage.getReason(),
            created = LocalDateTime.now(),
        )

        ashurBucketService.uploadBlob(
            "reports/${report.codespace}/filtering-report-${report.correlationId}.json",
            toJsonInputStream(report)
        )
    }
}