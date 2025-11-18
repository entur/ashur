package org.entur.ror.ashur.sax.plugins.filenames

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class FileNameRepositoryTest {
    @Test
    fun testAddFileToRename() {
        val repo = FileNameRepository()
        repo.addFileToRename("oldName.xml", "newName.xml")
        assertEquals("newName.xml", repo.filesToRename["oldName.xml"])
    }
}