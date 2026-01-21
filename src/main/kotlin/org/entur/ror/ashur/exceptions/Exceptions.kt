package org.entur.ror.ashur.exceptions

abstract class AshurException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

class InvalidZipFileException(message: String) : AshurException(message)
class InvalidFilterProfileException(message: String) : AshurException(message)
class NoJourneysInNetexFileException(message: String) : AshurException(message)
