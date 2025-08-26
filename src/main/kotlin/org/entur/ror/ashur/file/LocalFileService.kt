package org.entur.ror.ashur.file

import org.entur.ror.ashur.config.AppConfig
import org.entur.ror.ashur.createFileWithDirectories
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.io.File
import java.util.UUID

@Profile("local")
@Component
class LocalFileService(private val appConfig: AppConfig): FileService() {

    private val logger = LoggerFactory.getLogger(javaClass)

    private fun getPathToFileInBlobstore(fileName: String): String {
        return "${appConfig.gcp.bucketPath}/$fileName"
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
        val file = File(fileName)
        if (file.exists()) {
            val uniqueFileName = "$fileName-${UUID.randomUUID()}.${file.extension}"
            logger.warn("File $fileName already exists on local file system. Writing to file $uniqueFileName instead.")
            val uniqueFile = File(uniqueFileName)
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