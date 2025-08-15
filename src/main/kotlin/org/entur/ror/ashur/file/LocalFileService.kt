package org.entur.ror.ashur.file

import org.entur.ror.ashur.createFileWithDirectories
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.io.File
import java.util.UUID

@Profile("local")
@Component
class LocalFileService: FileService() {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun fileExists(fileName: String): Boolean {
        val fileExistsOnFileSystem = File(fileName).exists()
        val fileExistsOnClasspath = javaClass.classLoader.getResource(fileName) != null

        if (fileExistsOnFileSystem || fileExistsOnClasspath) {
            logger.info("File exists: $fileName")
            return true
        }

        logger.info("File does not exist: $fileName")
        return false
    }

    override fun getFileAsByteArray(fileName: String): ByteArray {
        val fileFromFileSystem = File(fileName)
        val fileFromClassPath = javaClass.classLoader.getResource(fileName)

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
            val uniqueFileName = "$fileName-${UUID.randomUUID()}.zip"
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