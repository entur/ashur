package org.entur.ror.ashur.utils

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class FileUtilsTest {

    @Test
    fun createDirectory() {
        val testDirectoryPath = "testDirectory"
        val directory = FileUtils.createDirectory(testDirectoryPath)

        try {
            assertTrue(directory.exists(), "Directory should exist after creation")
            assertTrue(directory.isDirectory, "Created file should be a directory")
        } finally {
            directory.deleteRecursively()
        }
    }
}