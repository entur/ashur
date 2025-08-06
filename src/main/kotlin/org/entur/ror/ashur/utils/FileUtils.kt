package org.entur.ror.ashur.utils

import java.io.File

object FileUtils {
    fun createDirectory(directory: String): File {
        val file = File(directory)
        file.mkdirs()
        return file
    }
}