package org.entur.ror.ashur

import org.entur.netex.tools.pipeline.app.FilterNetexApp
import org.entur.netex.tools.pipeline.config.JsonConfig
import org.entur.ror.ashur.exceptions.InvalidZipFileException
import org.entur.ror.ashur.utils.FileUtils
import org.entur.ror.ashur.file.FileService
import org.entur.ror.ashur.utils.ZipUtils
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Service for filtering Netex files from a zip archive.
 */
class FilterService(
    private val fileService: FileService,
    private val cleanUpEnabled: Boolean = true
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
     * Filters a Netex file from a zip archive and returns the filtered zip file.
     *
     * @param unfilteredNetexZipFile ByteArray of the zip file containing Netex files
     * @param inputDirectory The directory where the input files will be extracted.
     * @param outputDirectory The directory where the filtered output files will be saved.
     * @return The filtered zip file containing the processed Netex data.
     */
    private fun filterNetexToZipFile(
        unfilteredNetexZipFile: ByteArray,
        fileNameOfUnfilteredNetexZipFile: String,
        inputDirectory: File,
        outputDirectory: File,
    ): File {
        logger.info("Unzipping Netex file: $fileNameOfUnfilteredNetexZipFile")
        ZipUtils.unzipToDirectory(unfilteredNetexZipFile, inputDirectory)

        FilterNetexApp(
            // TODO: Adjust config such that config values come from a pubsub message rather than configuration file
            config = JsonConfig.load(javaClass.classLoader.getResource("config.json")!!.openStream()),
            input = inputDirectory,
            target = outputDirectory,
        ).run()

        val outputZipFile = File("$outputDirectory/filtered_$fileNameOfUnfilteredNetexZipFile")
        ZipUtils.zipDirectory(
            outputDirectory,
            outputZipFile,
        )
        logger.info("Zipped contents to file: ${outputZipFile.path}")

        return outputZipFile
    }

    /**
     * Handles the filtering request for a file.
     *
     * @param fileName The name of the file to filter.
     * @param inputDirectory The directory where the input files are located.
     * @param outputDirectory The directory where the output files will be saved.
     * @throws InvalidZipFileException If the file is invalid or empty.
     */
    fun handleFilterRequestForFile(
        fileName: String?,
        inputDirectory: String,
        outputDirectory: String,
    ) {
        if (fileName == null || fileName.isBlank() || fileName.isEmpty()) {
            throw InvalidZipFileException("File name cannot be null or blank")
        }
        if (fileService.fileExists(fileName)) {
            val inputZipFile = fileService.getFileAsByteArray(fileName)
            if (inputZipFile.isEmpty()) {
                throw InvalidZipFileException("Zip file is empty: $fileName")
            }

            val (directoryForInputFiles, directoryForOutputFiles) = createDirectories(inputDirectory, outputDirectory)

            val filteredNetexZipFile = filterNetexToZipFile(
                unfilteredNetexZipFile = inputZipFile,
                fileNameOfUnfilteredNetexZipFile = fileName,
                inputDirectory = directoryForInputFiles,
                outputDirectory = directoryForOutputFiles,
            )

            if (cleanUpEnabled) {
                cleanUpFiles(
                    directoryForInputFiles = directoryForInputFiles,
                    directoryForOutputFiles = directoryForOutputFiles,
                    filteredNetexZipFile = filteredNetexZipFile,
                )
            }
        }
    }
}