package org.entur.ror.ashur.pubsub

/**
 * The ephemeral lease serialized into `claims/{codespace}/{correlationId}`. It exists only to
 * serialize the in-flight window; it has no state machine and is never deleted by the app (a GCS
 * lifecycle rule GCs it). [startedAtEpochMs] drives the staleness/TTL check; [owner] and [attempt]
 * are for debugging/observability only.
 */
data class Claim(
    val owner: String,
    val startedAtEpochMs: Long,
    val attempt: Int,
)
