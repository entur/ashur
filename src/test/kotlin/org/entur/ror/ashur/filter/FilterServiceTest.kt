package org.entur.ror.ashur.filter

import org.entur.netex.tools.lib.report.FilterReport
import org.entur.ror.ashur.config.AppConfig
import org.entur.ror.ashur.config.PubSubEmulatorTestBase
import org.entur.ror.ashur.file.AshurBucketService
import org.entur.ror.ashur.file.MardukBucketService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.io.File
import kotlin.test.Test

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
        Assertions.assertTrue(file1.exists())
        Assertions.assertFalse(file2.exists())
        Assertions.assertTrue(sharedFile.exists())
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
        Assertions.assertTrue(filesToRemove.contains(file3))
        Assertions.assertFalse(filesToRemove.contains(file1))
        Assertions.assertFalse(filesToRemove.contains(file2))
        Assertions.assertFalse(filesToRemove.contains(sharedFile))
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
        Assertions.assertTrue(filesToKeep.contains(file1))
        Assertions.assertTrue(filesToKeep.contains(file2))
        Assertions.assertTrue(filesToKeep.contains(sharedFile))
        Assertions.assertFalse(filesToKeep.contains(file3))
    }

    @Test
    fun testHasNoJourneysInFilteredDataset() {
        val file1 = File("file1.xml")
        val file2 = File("file2.xml")
        val filterReport = FilterReport(
            mapOf(
                file1 to mutableMapOf(),
                file2 to mutableMapOf()
            ),
            emptyMap(),
        )
        Assertions.assertTrue(filterService.hasNoJourneysInFilteredDataset(filterReport))
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

        Assertions.assertFalse(filterService.hasNoJourneyInFile(filterReport, file1))
        Assertions.assertFalse(filterService.hasNoJourneyInFile(filterReport, file2))
        Assertions.assertTrue(filterService.hasNoJourneyInFile(filterReport, file3))
    }

    @Test
    fun testIsLineFile() {
        Assertions.assertTrue(filterService.isLineFile(File("line1.xml")))
        Assertions.assertFalse(filterService.isLineFile(File("_shared.xml")))
    }
}