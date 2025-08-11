package org.entur.ror.ashur.utils

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File
import java.util.zip.ZipFile

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
}