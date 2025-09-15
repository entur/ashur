package org.entur.ror.ashur.filter

import org.entur.netex.tools.lib.config.FilterConfig
import org.entur.netex.tools.pipeline.app.FilterNetexApp
import org.entur.ror.ashur.config.AppConfig
import org.entur.ror.ashur.exceptions.InvalidZipFileException
import org.entur.ror.ashur.file.AshurBucketService
import org.entur.ror.ashur.file.MardukBucketService
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
    private val ashurBucketService: AshurBucketService,
    private val mardukBucketService: MardukBucketService,
    private val appConfig: AppConfig,
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
     */
    private fun cleanUpFiles(
        directoryForInputFiles: File,
        directoryForOutputFiles: File,
    ) {
        logger.info("Cleaning up files in local file system...")
        directoryForInputFiles.deleteRecursively()
        directoryForOutputFiles.listFiles()?.forEach { file ->
            file.delete()
        }
        logger.info("Successfully cleaned up files in local file system.")
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
        entities.sorted().joinToString("\n").byteInputStream().use { stream ->
            ashurBucketService.uploadBlob("${uploadPath}/entities.txt", stream)
        }

        refs.sorted().joinToString("\n").byteInputStream().use { stream ->
            ashurBucketService.uploadBlob("${uploadPath}/refs.txt", stream)
        }
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
        val unfilteredNetexZipFile = mardukBucketService.getBlob(netexInputFilePath)
            ?: throw InvalidZipFileException("Could not retrieve file from Marduk bucket: $netexInputFilePath")

        logger.info("Unzipping Netex file: $netexInputFilePath")
        unfilteredNetexZipFile.use { inputStream ->
            ZipUtils.unzipToDirectory(inputStream, inputDirectory)
        }

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
        if (!mardukBucketService.exists(fileName)) {
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
     * @return The path of the filtered zip file in the Ashur bucket.
     * @throws org.entur.ror.ashur.exceptions.InvalidZipFileException If the file is invalid or empty.
     */
    fun handleFilterRequestForFile(
        fileName: String?,
        filterConfig: FilterConfig,
        codespace: String,
        correlationId: String,
        netexSource: String,
    ): String {
        val netexInputFile = getZipFile(fileName)
        val localPathForInputFiles = getPathForNetexInputFiles(codespace, correlationId, netexSource)
        val localPathForOutputFiles = getPathForNetexOutputFiles(codespace, correlationId, netexSource)
        val (localDirectoryForInputFiles, localDirectoryForOutputFiles) = createDirectories(
            localPathForInputFiles,
            localPathForOutputFiles
        )

        val uploadPath = "${codespace}/${correlationId}/$netexSource"
        logger.info("Starting filtering process for file: ${netexInputFile.name}")
        val filteredNetexZipFile = filterNetexToZipFile(
            netexInputFile = netexInputFile,
            inputDirectory = localDirectoryForInputFiles,
            outputDirectory = localDirectoryForOutputFiles,
            filterConfig = filterConfig,
            uploadPath = uploadPath
        )
        logger.info("Filtering process for file ${netexInputFile.name} was successful")

        val filteredZipFileName = "${uploadPath}/filtered_${netexInputFile.name}"
        logger.info("Uploading filtered Netex zip file to Ashur bucket")
        filteredNetexZipFile.inputStream().use { inputStream ->
            ashurBucketService.uploadBlob(
                filteredZipFileName,
                inputStream,
            )
        }
        logger.info("Successfully uploaded filtered Netex zip file. Path in bucket: $filteredZipFileName")

        if (appConfig.netex.cleanupEnabled) {
            cleanUpFiles(
                directoryForInputFiles = localDirectoryForInputFiles,
                directoryForOutputFiles = localDirectoryForOutputFiles,
            )
            netexInputFile.delete()
        }

        return filteredZipFileName
    }
}