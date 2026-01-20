package org.entur.ror.ashur

object Constants {
    val FILTER_NETEX_FILE_SUBSCRIPTION = "FilterNetexFileQueue"
    val FILTER_NETEX_FILE_STATUS_TOPIC = "FilterNetexFileStatusQueue"

    val FILTER_NETEX_FILE_STATUS_STARTED = "STARTED"
    val FILTER_NETEX_FILE_STATUS_SUCCEEDED = "SUCCESS"
    val FILTER_NETEX_FILE_STATUS_FAILED = "FAILED"

    val FILE_CREATED_TIMESTAMP_HEADER = "FileCreatedTimestamp"

    val FILTERED_NETEX_FILE_PATH_HEADER = "FilteredNetexFilePath"

    val CORRELATION_ID_HEADER = "RutebankenCorrelationId"

    val FILTERING_REPORT_STATUS_HEADER = "FilteringReportStatus"
    val FILTERING_FAILURE_REASON_HEADER = "FilteringFailureReason"
}
