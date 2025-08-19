package org.entur.ror.ashur.filter

import org.entur.netex.tools.lib.config.FilterConfig
import org.entur.netex.tools.pipeline.app.FilterNetexApp
import org.entur.ror.ashur.config.AppConfig
import org.entur.ror.ashur.exceptions.InvalidZipFileException
import org.entur.ror.ashur.file.FileService
import org.entur.ror.ashur.utils.FileUtils
import org.entur.ror.ashur.utils.ZipUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.File

/**
 * Service for filtering Netex files from a zip archive.
 */
@Component
class FilterService(
    private val fileService: FileService,
    private val appConfig: AppConfig
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Creates the input and output directories for the filtering process.
     *
     * @param inputDirectory The directory where input files will be stored.
     * @param outputDirectory The directory where output files will be saved.
     * @return A pair of File objects representing the created input and output directories.
     */
    private fun createDirectories(inputDirectory: String, outputDirectory: String): Pair<File, File> {
        logger.info("Creating input directory: $inputDirectory")
        val directoryForInputFiles = FileUtils.createDirectory(inputDirectory)

        logger.info("Creating output directory: $outputDirectory")
        val directoryForOutputFiles = FileUtils.createDirectory(outputDirectory)

        return Pair(directoryForInputFiles, directoryForOutputFiles)
    }

    /**
     * Cleans up the input and output directories by deleting the files, excluding the output zip file.
     *
     * @param directoryForInputFiles The directory containing input files.
     * @param directoryForOutputFiles The directory containing output files.
     * @param filteredNetexZipFile The zip file created as output.
     */
    private fun cleanUpFiles(
        directoryForInputFiles: File,
        directoryForOutputFiles: File,
        filteredNetexZipFile: File
    ) {
        logger.info("Cleaning up files...")
        directoryForInputFiles.deleteRecursively()
        directoryForOutputFiles.listFiles()?.forEach { file ->
            if (file.path != filteredNetexZipFile.path) {
                file.delete()
            }
        }
        logger.info("Successfully cleaned up files.")
    }

    /**
     * Uploads the kept entities and references to .txt files through fileService
     *
     * @param entities The set of entity IDs to upload.
     * @param refs The set of reference IDs to upload.
     * @param codespace The codespace identifier.
     * @param correlationId The correlation ID for the operation.
     */
    fun uploadKeptEntitiesAndRefsExports(
        entities: Set<String>,
        refs: Set<String>,
        uploadPath: String,
    ) {
        fileService.uploadFile(
            "${uploadPath}/entities.txt",
            entities.sorted().joinToString("\n").toByteArray()
        )

        fileService.uploadFile(
            "${uploadPath}/refs.txt",
            refs.sorted().joinToString("\n").toByteArray()
        )
    }

    /**
     * Filters a Netex file from a zip archive and returns the filtered zip file.
     *
     * @param inputNetexFileName Name of the Netex file to filter.
     * @param inputDirectory The directory where the input files will be extracted.
     * @param outputDirectory The directory where the filtered output files will be saved.
     * @return The filtered zip file containing the processed Netex data.
     */
    private fun filterNetexToZipFile(
        netexInputFile: File,
        inputDirectory: File,
        outputDirectory: File,
        filterConfig: FilterConfig,
        uploadPath: String,
    ): File {
        val netexInputFilePath = netexInputFile.path
        val unfilteredNetexZipFile = fileService.getFileAsByteArray(netexInputFilePath)
        if (unfilteredNetexZipFile.isEmpty()) {
            throw InvalidZipFileException("Zip file is empty: $netexInputFilePath")
        }

        logger.info("Unzipping Netex file: $netexInputFilePath")
        ZipUtils.unzipToDirectory(unfilteredNetexZipFile, inputDirectory)

        val (entities, refs) = FilterNetexApp(
            filterConfig = filterConfig,
            input = inputDirectory,
            target = outputDirectory,
        ).run()

        uploadKeptEntitiesAndRefsExports(
            entities = entities,
            refs = refs,
            uploadPath = uploadPath,
        )

        val outputZipFile = File("$outputDirectory/filtered_${netexInputFile.name}")
        ZipUtils.zipDirectory(
            outputDirectory,
            outputZipFile,
        )
        logger.info("Zipped contents to file: ${outputZipFile.path}")

        return outputZipFile
    }

    /**
     * Constructs the path for the input directory based on the codespace and correlation ID.
     *
     * @param codespace The codespace identifier
     * @param correlationId The correlation ID
     * @return The path of the input directory for the specified message.
     */
    fun getPathForNetexInputFiles(codespace: String, correlationId: String, netexSource: String): String {
        return "${appConfig.netex.inputPath}/${codespace}/${correlationId}/${netexSource}"
    }

    /**
     * Constructs the path for the output directory based on the codespace and correlation ID.
     *
     * @param codespace The codespace identifier
     * @param correlationId The correlation ID
     * @return The path of the output directory for the specified message.
     */
    fun getPathForNetexOutputFiles(codespace: String, correlationId: String, netexSource: String): String {
        return "${appConfig.netex.outputPath}/${codespace}/${correlationId}/${netexSource}"
    }

    private fun validateZipFile(fileName: String?): File {
        if (fileName == null || fileName.isBlank() || fileName.isEmpty()) {
            throw InvalidZipFileException("File name cannot be null or blank")
        }
        if (!fileService.fileExists(fileName)) {
            throw InvalidZipFileException("File does not exist: $fileName")
        }
        return File(fileName)
    }

    private fun getZipFile(fileName: String?): File {
        val file = validateZipFile(fileName)
        return file
    }

    /**
     * Handles the filtering request for a file.
     *
     * @param fileName The name of the file to filter.
     * @param inputDirectory The directory where the input files are located.
     * @param outputDirectory The directory where the output files will be saved.
     * @throws org.entur.ror.ashur.exceptions.InvalidZipFileException If the file is invalid or empty.
     */
    fun handleFilterRequestForFile(
        fileName: String?,
        filterConfig: FilterConfig,
        codespace: String,
        correlationId: String,
        netexSource: String,
    ) {
        val netexInputFile = getZipFile(fileName)
        val localPathForInputFiles = getPathForNetexInputFiles(codespace, correlationId, netexSource)
        val localPathForOutputFiles = getPathForNetexOutputFiles(codespace, correlationId, netexSource)
        val (localDirectoryForInputFiles, localDirectoryForOutputFiles) = createDirectories(
            localPathForInputFiles,
            localPathForOutputFiles
        )

        val uploadPath = "${appConfig.gcp.bucketPath}/${codespace}/${correlationId}/$netexSource"
        val filteredNetexZipFile = filterNetexToZipFile(
            netexInputFile = netexInputFile,
            inputDirectory = localDirectoryForInputFiles,
            outputDirectory = localDirectoryForOutputFiles,
            filterConfig = filterConfig,
            uploadPath = uploadPath
        )

        if (appConfig.netex.cleanupEnabled) {
            cleanUpFiles(
                directoryForInputFiles = localDirectoryForInputFiles,
                directoryForOutputFiles = localDirectoryForOutputFiles,
                filteredNetexZipFile = filteredNetexZipFile,
            )
        }

        fileService.uploadFile(
            "${uploadPath}/filtered_${netexInputFile.name}",
            filteredNetexZipFile.readBytes()
        )
    }
}