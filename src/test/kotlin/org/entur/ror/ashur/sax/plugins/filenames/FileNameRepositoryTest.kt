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

    @Test
    fun testIdempotentAddDoesNotTriggerCollision() {
        val repo = FileNameRepository()
        repo.addFileToRename("oldName.xml", "newName.xml")
        repo.addFileToRename("oldName.xml", "newName.xml")
        assertEquals("newName.xml", repo.filesToRename["oldName.xml"])
        assertEquals(1, repo.filesToRename.size)
    }

    @Test
    fun testCollisionAppendsCounterSuffix() {
        val repo = FileNameRepository()
        repo.addFileToRename("a.xml", "target.xml")
        repo.addFileToRename("b.xml", "target.xml")
        assertEquals("target.xml", repo.filesToRename["a.xml"])
        assertEquals("target_2.xml", repo.filesToRename["b.xml"])
    }

    @Test
    fun testThirdCollisionIncrementsCounter() {
        val repo = FileNameRepository()
        repo.addFileToRename("a.xml", "target.xml")
        repo.addFileToRename("b.xml", "target.xml")
        repo.addFileToRename("c.xml", "target.xml")
        assertEquals("target.xml", repo.filesToRename["a.xml"])
        assertEquals("target_2.xml", repo.filesToRename["b.xml"])
        assertEquals("target_3.xml", repo.filesToRename["c.xml"])
    }

    @Test
    fun testCollisionPreservesXmlExtension() {
        val repo = FileNameRepository()
        repo.addFileToRename("a.xml", "complex_name-with-dashes.xml")
        repo.addFileToRename("b.xml", "complex_name-with-dashes.xml")
        assertEquals("complex_name-with-dashes_2.xml", repo.filesToRename["b.xml"])
    }
}
