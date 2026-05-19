package org.entur.ror.ashur.sax.plugins.filenames

import org.slf4j.LoggerFactory

data class FileNameRepository(
    val filesToRename: MutableMap<String, String> = mutableMapOf()
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun addFileToRename(previousName: String, newName: String) {
        val resolved = if (filesToRename.containsValue(newName) && filesToRename[previousName] != newName) {
            val base = newName.removeSuffix(".xml")
            val unique = generateSequence(2) { it + 1 }
                .map { "${base}_$it.xml" }
                .first { it !in filesToRename.values }
            logger.warn(
                "Filename collision: '{}' already mapped; using '{}' for '{}'",
                newName, unique, previousName
            )
            unique
        } else {
            newName
        }
        filesToRename[previousName] = resolved
    }
}
