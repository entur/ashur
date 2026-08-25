package org.entur.ror.ashur.pubsub

/**
 * The lease serialized into `claims/{codespace}/{correlationId}/{filterProfile}`. The profile is part
 * of the path because one import run reuses a single correlationId across several profiles, and those
 * are distinct units of work producing distinct artifacts. It is never deleted by the app (a
 * GCS lifecycle rule GCs it eventually). [startedAtEpochMs] drives the staleness/TTL check for a
 * claim that isn't [completed] yet; [owner] and [attempt] are for debugging/observability only.
 *
 * [completed] is the guard's sole "done" signal: it is set only once every externally-visible effect
 * of the run (bucket upload, exchange-bucket copy, status publish) has actually happened, so a future
 * delivery can trust it to SKIP for good, regardless of how stale the claim gets.
 */
data class Claim(
    val owner: String,
    val startedAtEpochMs: Long,
    val attempt: Int,
    val completed: Boolean = false,
)
