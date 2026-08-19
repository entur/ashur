package org.entur.ror.ashur

object Constants {
    val FILTER_NETEX_FILE_SUBSCRIPTION = "FilterNetexFileQueue"
    val FILTER_NETEX_FILE_STATUS_TOPIC = "FilterNetexFileStatusQueue"

    val FILTER_NETEX_FILE_STATUS_STARTED = "STARTED"
    val FILTER_NETEX_FILE_STATUS_SUCCEEDED = "SUCCESS"
    val FILTER_NETEX_FILE_STATUS_FAILED = "FAILED"

    val FILE_CREATED_TIMESTAMP_HEADER = "FileCreatedTimestamp"
    val FILTERED_NETEX_FILE_PATH_HEADER = "FilteredNetexFilePath"
    val FILTERING_PROFILE_HEADER = "EnturFilteringProfile"
    val NETEX_FILE_NAME_HEADER = "RutebankenTargetFileHandle"
    val CORRELATION_ID_HEADER = "RutebankenCorrelationId"
    val CODESPACE_HEADER = "EnturDatasetReferential"
    val NETEX_SOURCE_HEADER = "NetexSource"

    val FILTER_REPORT_HEADER = "FilterReport"
    val FILTERING_REPORT_STATUS_HEADER = "Status"
    val FILTERING_FAILURE_REASON_HEADER = "FilteringFailureReason"
    val FILTERING_ERROR_CODE_HEADER = "FilteringErrorCode"

    val NO_JOURNEYS_IN_NETEX_DATASET_ERROR_CODE = "NO_JOURNEYS_IN_NETEX_DATASET"

    /**
     * Stand-in used by the filtering pipeline for the output path when a request carries no
     * correlationId. Deliberately NOT used as a redelivery-guard claim key: as a placeholder it is
     * shared by every such request for a codespace+profile, so a completed claim under it would
     * silently skip unrelated requests. Those deliveries run unguarded instead — see
     * [org.entur.ror.ashur.pubsub.RedeliveryGuard.evaluate].
     */
    val UNKNOWN_CORRELATION_ID = "unknown"

    val GUARD_DECISION_HEADER = "AshurRedeliveryGuardDecision"
    val GUARD_DECISION_SKIP = "SKIP"
    /** Internal-only exchange header carrying the [org.entur.ror.ashur.pubsub.ClaimHandle] from the guard to [org.entur.ror.ashur.camel.RedeliveryGuardCompletionProcessor]; never sent as a Pub/Sub attribute. */
    val CLAIM_HANDLE_HEADER = "AshurRedeliveryGuardClaimHandle"
}
