package org.entur.ror.ashur.utils

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object ZipUtils {
    /**
     * Unzips a byte array containing a zip file into a specified directory.
     *
     * @param zipBytes The byte array containing the zip file.
     * @param targetDirectory The directory where the contents of the zip file will be extracted.
     * @return A list of files that were extracted from the zip file.
     */
    fun unzipToDirectory(input: InputStream, targetDirectory: File) {
        ZipInputStream(input).use { zipInputStream ->
            var entry = zipInputStream.nextEntry
            while (entry != null) {
                val outFile = File(targetDirectory, entry.name)
                if (entry.isDirectory) {
                    outFile.mkdirs()
                } else {
                    outFile.parentFile?.mkdirs()
                    FileOutputStream(outFile).use { fileOut ->
                        zipInputStream.copyTo(fileOut)
                    }
                }
                zipInputStream.closeEntry()
                entry = zipInputStream.nextEntry
            }
        }
    }

    /**
     * Zips the contents of a directory into a zip file.
     *
     * @param sourceDirectory The directory whose contents will be zipped.
     * @param zipFile The file where the zip archive will be created.
     */
    fun zipDirectory(sourceDirectory: File, zipFile: File) {
        FileOutputStream(zipFile).use { fileOutputStream ->
            ZipOutputStream(fileOutputStream).use { zipOutputStream ->
                sourceDirectory.walkTopDown().forEach { file ->
                    if (file.isFile && file != zipFile) {
                        val entryName = sourceDirectory.toPath().relativize(file.toPath()).toString()
                        zipOutputStream.putNextEntry(ZipEntry(entryName))
                        file.inputStream().use { it.copyTo(zipOutputStream) }
                        zipOutputStream.closeEntry()
                    }
                }
            }
        }
    }
}