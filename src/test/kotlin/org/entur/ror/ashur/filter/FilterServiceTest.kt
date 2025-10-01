package org.entur.ror.ashur.filter

import org.entur.netex.tools.lib.report.FilterReport
import org.entur.ror.ashur.config.AppConfig
import org.entur.ror.ashur.config.PubSubEmulatorTestBase
import org.entur.ror.ashur.file.AshurBucketService
import org.entur.ror.ashur.file.MardukBucketService
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.io.File
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertTrue

@SpringBootTest
class FilterServiceTest(@Autowired var filterService: FilterService) : PubSubEmulatorTestBase() {

    @Autowired
    lateinit var ashurBucketService: AshurBucketService

    @Autowired
    lateinit var mardukBucketService: MardukBucketService

    @Autowired
    lateinit var appConfig: AppConfig

    @BeforeEach
    fun setUp() {
        filterService = FilterService(ashurBucketService, mardukBucketService, appConfig)
    }

    @Test
    fun testRemoveLineFilesToRemove() {
        val file1 = File.createTempFile("file1", ".xml").also { it.deleteOnExit() }
        val file2 = File.createTempFile("file2", ".xml").also { it.deleteOnExit() }
        val sharedFile = File.createTempFile("_shared", ".xml").also { it.deleteOnExit() }

        val filterReport = FilterReport(
            mapOf(
                file1 to mutableMapOf("ServiceJourney" to 1),
                file2 to mutableMapOf(),
                sharedFile to mutableMapOf()
            ),
            emptyMap(),
        )

        filterService.removeLineFilesToRemove(filterReport)
        assertTrue(file1.exists())
        assertFalse(file2.exists())
        assertTrue(sharedFile.exists())
    }

    @Test
    fun testFindLineFilesToRemove() {
        val file1 = File("file1.xml")
        val file2 = File("file2.xml")
        val file3 = File("file3.xml")
        val sharedFile = File("_shared.xml")
        val filterReport = FilterReport(
            mapOf(
                file1 to mutableMapOf("ServiceJourney" to 1),
                file2 to mutableMapOf("DatedServiceJourney" to 1),
                file3 to mutableMapOf(),
                sharedFile to mutableMapOf()
            ),
            emptyMap(),
        )

        val filesToRemove = filterService.findLineFilesToRemove(filterReport)
        assertContains(filesToRemove, file3)
        assertFalse(filesToRemove.contains(file1))
        assertFalse(filesToRemove.contains(file2))
        assertFalse(filesToRemove.contains(sharedFile))
    }

    @Test
    fun testFilesToKeep() {
        val file1 = File("file1.xml")
        val file2 = File("file2.xml")
        val file3 = File("file3.xml")
        val sharedFile = File("_shared.xml")
        val filterReport = FilterReport(
            mapOf(
                file1 to mutableMapOf("ServiceJourney" to 1),
                file2 to mutableMapOf("DatedServiceJourney" to 1),
                file3 to mutableMapOf(),
                sharedFile to mutableMapOf()
            ),
            emptyMap(),
        )

        val filesToKeep = filterService.findFilesToKeep(filterReport)
        assertContains(filesToKeep, file1)
        assertContains(filesToKeep, file2)
        assertContains(filesToKeep, sharedFile)
        assertFalse(filesToKeep.contains(file3))
    }

    @Test
    fun testHasNoJourneyInFile() {
        val file1 = File("file1.xml")
        val file2 = File("file2.xml")
        val file3 = File("file3.xml")
        val filterReport = FilterReport(
            mapOf(
                file1 to mutableMapOf("ServiceJourney" to 1),
                file2 to mutableMapOf("DatedServiceJourney" to 1),
                file3 to mutableMapOf()
            ),
            emptyMap(),
        )

        assertFalse(filterService.hasNoJourneyInFile(filterReport, file1))
        assertFalse(filterService.hasNoJourneyInFile(filterReport, file2))
        assertTrue(filterService.hasNoJourneyInFile(filterReport, file3))
    }

    @Test
    fun testIsLineFile() {
        assertTrue(filterService.isLineFile(File("line1.xml")))
        assertFalse(filterService.isLineFile(File("_shared.xml")))
    }
}