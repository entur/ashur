package org.entur.ror.ashur.exceptions

import org.entur.ror.ashur.Constants

abstract class AshurException(
    message: String,
    val errorCode: String? = null,
    cause: Throwable? = null
) : Exception(message, cause)

class InvalidZipFileException(message: String): Exception(message)
class InvalidFilterProfileException(message: String) : Exception(message)

/**
 * Signals that the request is already claimed by a live holder (a fresh claim, or a lost takeover
 * race). Deliberately NOT an [AshurException]: it must not be turned into a FAILED status. The route
 * handles it with `handled(false)`, so the message is nacked and Pub/Sub redelivers it later.
 */
class ClaimHeldException(message: String) : Exception(message)

class NoJourneysInNetexFileException(message: String) : AshurException(
    message = message,
    errorCode = Constants.NO_JOURNEYS_IN_NETEX_DATASET_ERROR_CODE,
)
