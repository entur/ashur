package org.entur.ror.ashur.utils

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class ZipUtilsTest {
    @Test
    fun testZipDirectory() {
        val testDirectory = File("tmpDir")
        try {
            if (!testDirectory.exists()) {
                testDirectory.mkdirs()
            }

            File(testDirectory, "file1.txt").apply { writeText("Content of file 1") }
            File(testDirectory, "file2.txt").apply { writeText("Content of file 2") }

            val zipFileAsFileObject = File(testDirectory, "testZip.zip")
            ZipUtils.zipDirectory(testDirectory, zipFileAsFileObject)
            assertTrue(zipFileAsFileObject.exists(), "Zip file should be created")

            val zipFile = ZipFile(zipFileAsFileObject)
            zipFile.getEntry("file1.txt").let {
                assertNotNull(it, "file1.txt should be present in the zip file")
                assertEquals("Content of file 1", zipFile.getInputStream(it).bufferedReader().use { it.readText() })
            }
            zipFile.getEntry("file2.txt").let {
                assertNotNull(it, "file2.txt should be present in the zip file")
                assertEquals("Content of file 2", zipFile.getInputStream(it).bufferedReader().use { it.readText() })
            }
        } finally {
            testDirectory.deleteRecursively()
        }
    }

    private fun zipFileToOutputStream(fileToZip: File, zipOutputStream: ZipOutputStream) {
        zipOutputStream.putNextEntry(ZipEntry(fileToZip.name))
        zipOutputStream.write(fileToZip.readBytes())
        zipOutputStream.closeEntry()
    }

    @Test
    fun testUnzipToDirectory() {
        val testDirectory = "test"
        val targetDirectory = File("$testDirectory/tmpDir")
        if (!targetDirectory.exists()) {
            targetDirectory.mkdirs()
        }

        try {
            val zipFile = File("$testDirectory/testZip.zip")
            val file1 = File(targetDirectory, "file1.txt").apply { writeText("Content of file 1") }
            val file2 = File(targetDirectory, "file2.txt").apply { writeText("Content of file 2") }
            ZipOutputStream(zipFile.outputStream()).use { zipOutputStream ->
                zipFileToOutputStream(file1, zipOutputStream)
                zipFileToOutputStream(file2, zipOutputStream)
            }

            file1.delete()
            file2.delete()

            ZipUtils.unzipToDirectory(zipFile.inputStream(), targetDirectory)
            val unzippedFile1 = File(targetDirectory, "file1.txt")
            val unzippedFile2 = File(targetDirectory, "file2.txt")
            assertTrue(unzippedFile1.exists(), "file1.txt should be unzipped")
            assertTrue(unzippedFile2.exists(), "file2.txt should be unzipped")
            assertEquals("Content of file 1", unzippedFile1.readText(), "Content of file 1 should match")
            assertEquals("Content of file 2", unzippedFile2.readText(), "Content of file 2 should match")

        } finally {
            File(testDirectory).deleteRecursively()
        }
    }
}