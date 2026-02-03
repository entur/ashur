package org.entur.ror.ashur.exceptions

import org.entur.ror.ashur.Constants

abstract class AshurException(
    message: String,
    val errorCode: String? = null,
    cause: Throwable? = null
) : Exception(message, cause)

class InvalidZipFileException(message: String): Exception(message)
class InvalidFilterProfileException(message: String) : Exception(message)

class NoJourneysInNetexFileException(message: String) : AshurException(
    message = message,
    errorCode = Constants.NO_JOURNEYS_IN_NETEX_DATASET_ERROR_CODE,
)
