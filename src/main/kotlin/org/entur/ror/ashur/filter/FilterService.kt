package org.entur.ror.ashur.filter

import org.entur.netex.tools.lib.config.FilterConfig
import org.entur.netex.tools.lib.report.FilterReport
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
     * Uploads the kept entities to a .txt file through fileService
     *
     * @param filterReport The filter report containing information about the entities and references.
     * @param uploadPath The path in the Ashur bucket where the files will be uploaded
     */
    fun uploadKeptEntitiesReport(
        filterReport: FilterReport,
        uploadPath: String,
    ) {
        val filesToKeep = findFilesToKeep(filterReport)
        val entityIds = filterReport.getAllEntityIdsByFiles(filesToKeep)
        entityIds.joinToString("\n").byteInputStream().use { stream ->
            ashurBucketService.uploadBlob("${uploadPath}/entities.txt", stream)
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
    ): Pair<File, FilterReport> {
        val netexInputFilePath = netexInputFile.path
        val unfilteredNetexZipFile = mardukBucketService.getBlob(netexInputFilePath)
            ?: throw InvalidZipFileException("Could not retrieve file from Marduk bucket: $netexInputFilePath")

        logger.info("Unzipping Netex file: $netexInputFilePath")
        unfilteredNetexZipFile.use { inputStream ->
            ZipUtils.unzipToDirectory(inputStream, inputDirectory)
        }

        val filterReport = FilterNetexApp(
            filterConfig = filterConfig,
            input = inputDirectory,
            target = outputDirectory,
        ).run()

        removeLineFilesToRemove(filterReport)

        val outputZipFile = File("$outputDirectory/filtered_${netexInputFile.name}")
        ZipUtils.zipDirectory(
            outputDirectory,
            outputZipFile,
        )
        logger.info("Zipped contents to file: ${outputZipFile.path}")

        return Pair(first = outputZipFile, second = filterReport)
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
     * Returns true if the given file does not contain any ServiceJourney or DatedServiceJourney entities.
     *
     * @param filterReport The filter report containing information about the entities in each file.
     * @param file The file to check.
     * @return True if the file has no ServiceJourney or DatedServiceJourney entities, false otherwise.
     * */
    fun hasNoJourneyInFile(filterReport: FilterReport, file: File): Boolean =
        filterReport.getNumberOfElementsByFile(file, "DatedServiceJourney") == 0 &&
        filterReport.getNumberOfElementsByFile(file, "ServiceJourney") == 0

    /**
     * Returns true if the given file is a line file (= file name does not start with an underscore).
     *
     * @param file The file to check.
     * @return True if the file is a line file, false otherwise.
     */
    fun isLineFile(file: File): Boolean = !file.name.startsWith("_")

    /**
     * Finds the files that can be removed based on the filter report.
     *
     * @param filterReport The filter report containing information about the entities in each file.
     * @return A set of file names that can be removed.
     */
    fun findLineFilesToRemove(filterReport: FilterReport): Set<File> {
        return filterReport.elementTypesByFile
            .filter { isLineFile(it.key) }
            .filter { hasNoJourneyInFile(filterReport, it.key) }.keys
    }

    /**
     * Finds the files that should be kept based on the filter report.
     *
     * @param filterReport The filter report containing information about the entities in each file.
     * @return A set of file names that should be kept.
     */
    fun findFilesToKeep(filterReport: FilterReport): Set<File> {
        val filesToRemove = findLineFilesToRemove(filterReport)
        return filterReport.elementTypesByFile.keys.subtract(filesToRemove).toSet()
    }

    /**
     * Removes the files that can be removed based on the filter report.
     *
     * @param filterReport The filter report containing information about the entities in each file.
     */
    fun removeLineFilesToRemove(filterReport: FilterReport) {
        val filesToRemove = findLineFilesToRemove(filterReport)
        filesToRemove.forEach { file ->
            logger.info("Removing file without journeys: ${file.name}")
            file.delete()
        }
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

        logger.info("Starting filtering process for file: ${netexInputFile.name}")
        val (filteredNetexZipFile, filterReport) = filterNetexToZipFile(
            netexInputFile = netexInputFile,
            inputDirectory = localDirectoryForInputFiles,
            outputDirectory = localDirectoryForOutputFiles,
            filterConfig = filterConfig,
        )
        logger.info("Filtering process for file ${netexInputFile.name} was successful")

        logger.info("Uploading file with ids of kept entities to Ashur bucket")
        val uploadPath = "${codespace}/${correlationId}/$netexSource"
        uploadKeptEntitiesReport(
            filterReport = filterReport,
            uploadPath = uploadPath,
        )
        logger.info("Successfully uploaded ids of kept entities to Ashur bucket")

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