package org.entur.ror.ashur.file

abstract class FileService {
    /**
     * Checks if a file exists in the storage.
     *
     * @param fileName The name of the file to check.
     * @param correlationId An optional correlation ID for logging purposes.
     * @return True if the file exists, false otherwise.
     */
    abstract fun fileExists(fileName: String): Boolean

    /**
     * Retrieves the content of a file as a byte array.
     *
     * @param fileName The name of the file to retrieve.
     * @return The content of the file as a byte array.
     */
    abstract fun getFileAsByteArray(fileName: String): ByteArray

    /**
     * Uploads a file with the given name and content.
     *
     * @param fileName The name of the file to upload.
     * @param content The content of the file as a byte array.
     * @return True if the upload was successful, false otherwise.
     */
    abstract fun uploadFile(fileName: String, content: ByteArray): Boolean
}