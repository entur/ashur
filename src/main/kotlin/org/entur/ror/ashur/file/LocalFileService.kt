package org.entur.ror.ashur.file

import org.entur.ror.ashur.createFileWithDirectories
import org.slf4j.LoggerFactory
import java.io.File
import java.util.UUID

abstract class LocalFileService(private val bucketPath: String): FileService() {
    private val logger = LoggerFactory.getLogger(javaClass)

    private fun getPathToFileInBlobstore(fileName: String): String {
        return "${bucketPath}/$fileName"
    }

    override fun fileExists(fileName: String): Boolean {
        val fileExistsOnFileSystem = File(fileName).exists()
        val fileExistsInLocalBlobstore = File(getPathToFileInBlobstore(fileName)).exists()
        val fileExistsOnClasspath = javaClass.classLoader.getResource(fileName) != null

        if (fileExistsOnFileSystem || fileExistsOnClasspath || fileExistsInLocalBlobstore) {
            logger.info("File exists: $fileName")
            return true
        }

        logger.info("File does not exist: $fileName")
        return false
    }

    override fun getFileAsByteArray(fileName: String): ByteArray {
        val fileFromFileSystem = File(fileName)
        val fileFromLocalBlobstore = File(getPathToFileInBlobstore(fileName))
        val fileFromClassPath = javaClass.classLoader.getResource(fileName)

        if (fileFromLocalBlobstore.exists()) {
            return fileFromLocalBlobstore.readBytes()
        }
        if (fileFromFileSystem.exists()) {
            return fileFromFileSystem.readBytes()
        }
        if (fileFromClassPath != null) {
            return fileFromClassPath.readBytes()
        }

        throw IllegalArgumentException("File not found: $fileName")
    }

    override fun uploadFile(fileName: String, content: ByteArray): Boolean {
        val filePathInBlobstore = getPathToFileInBlobstore(fileName)
        val file = File(filePathInBlobstore)
        if (file.exists()) {
            val uniqueFileName = "$fileName-${UUID.randomUUID()}.${file.extension}"
            logger.warn("File $fileName already exists on local file system. Writing to file $uniqueFileName instead.")
            val uniqueFile = File("${bucketPath}/${uniqueFileName}")
            uniqueFile.createFileWithDirectories()
            uniqueFile.writeBytes(content)
        } else {
            file.createFileWithDirectories()
            file.writeBytes(content)
        }
        logger.info("File uploaded successfully: $fileName")
        return true
    }
}