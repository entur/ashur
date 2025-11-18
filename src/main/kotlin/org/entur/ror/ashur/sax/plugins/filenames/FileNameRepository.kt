package org.entur.ror.ashur.sax.plugins.filenames

data class FileNameRepository(
    val filesToRename: MutableMap<String, String> = mutableMapOf()
) {
    fun addFileToRename(previousName: String, newName: String) {
        filesToRename[previousName] = newName
    }
}