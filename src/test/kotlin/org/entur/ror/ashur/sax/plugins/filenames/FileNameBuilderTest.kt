package org.entur.ror.ashur.sax.plugins.filenames

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class FileNameBuilderTest {
    private val fileNameBuilder = FileNameBuilder()

    @Test
    fun withCodespace() {
        val codespace = "TestCodespace"
        fileNameBuilder.withCodespace(codespace)
        assertEquals(codespace, fileNameBuilder.codespace)
    }

    @Test
    fun withLineType() {
        val lineType = "TestLineType"
        fileNameBuilder.withLineType(lineType)
        assertEquals(lineType, fileNameBuilder.lineType)
    }

    @Test
    fun withLineName() {
        val lineName = "TestLineName"
        fileNameBuilder.withLineName(lineName)
        assertEquals(lineName, fileNameBuilder.lineName)
    }

    @Test
    fun withLineId() {
        val lineId = "TestLineId"
        fileNameBuilder.withLineId(lineId)
        assertEquals(lineId, fileNameBuilder.lineId)
    }

    @Test
    fun withLinePublicCode() {
        val publicCode = "TestPublicCode"
        fileNameBuilder.withLinePublicCode(publicCode)
        assertEquals(publicCode, fileNameBuilder.linePublicCode)
    }

    @Test
    fun withLinePrivateCode() {
        val privateCode = "TestPrivateCode"
        fileNameBuilder.withLinePrivateCode(privateCode)
        assertEquals(privateCode, fileNameBuilder.linePrivateCode)
    }

    @Test
    fun build() {
        val codespace = "TestCodespace"
        val lineType = "TestLineType"
        val lineName = "TestLineName"
        val publicCode = "TestPublicCode"
        val privateCode = "TestPrivateCode"

        val expectedFileName = "TESTCODESPACE_TESTCODESPACE-TestLineType-TestPrivateCode_TestPublicCode_TestLineName.xml"

        val fileName = fileNameBuilder
            .withCodespace(codespace)
            .withLineType(lineType)
            .withLineName(lineName)
            .withLinePublicCode(publicCode)
            .withLinePrivateCode(privateCode)
            .build()

        assertEquals(expectedFileName, fileName)
    }

    @Test
    fun testBuildWithEmptyPublicCode() {
        val codespace = "TestCodespace"
        val lineType = "TestLineType"
        val lineName = "TestLineName"
        val lineId = "AVI:Line:WF_ALF-VDS"

        val expectedFileName = "TESTCODESPACE_AVI-Line-WF_ALF-VDS_TestLineName.xml"

        val fileName = fileNameBuilder
            .withCodespace(codespace)
            .withLineType(lineType)
            .withLineName(lineName)
            .withLineId(lineId)
            .build()

        assertEquals(expectedFileName, fileName)
    }

    @Test
    fun testBuildWithEmptyPrivateCode() {
        val codespace = "TestCodespace"
        val lineType = "TestLineType"
        val lineName = "TestLineName"
        val publicCode = "TestPublicCode"
        val privateCode = ""

        val expectedFileName = "TESTCODESPACE_TESTCODESPACE-TestLineType-TestPublicCode_TestPublicCode_TestLineName.xml"

        val fileName = fileNameBuilder
            .withCodespace(codespace)
            .withLineType(lineType)
            .withLineName(lineName)
            .withLinePublicCode(publicCode)
            .withLinePrivateCode(privateCode)
            .build()

        assertEquals(expectedFileName, fileName)
    }

    @Test
    fun sanitizesNordicAndUmlautDiacritics() {
        val fileName = fileNameBuilder
            .withCodespace("TST")
            .withLineType("Line")
            .withLineName("Brösarp Ängelholm Sætre Øvre Ålesund")
            .withLinePublicCode("1")
            .withLinePrivateCode("X")
            .build()

        assertEquals(
            "TST_TST-Line-X_1_Brosarp-Angelholm-Setre-Ovre-Alesund.xml",
            fileName,
        )
    }

    @Test
    fun sanitizesFilesystemUnsafeCharacters() {
        val fileName = fileNameBuilder
            .withCodespace("TST")
            .withLineType("Line")
            .withLineName("A:B\\C<D>E\"F|G?H*I;J")
            .withLinePublicCode("1")
            .withLinePrivateCode("X")
            .build()

        assertEquals(
            "TST_TST-Line-X_1_A-B-C-D-E-F-G-H-I-J.xml",
            fileName,
        )
    }

    @Test
    fun sanitizesPreviouslyHandledCharacters() {
        val fileName = fileNameBuilder
            .withCodespace("TST")
            .withLineType("Line")
            .withLineName("It's a/b.c d")
            .withLinePublicCode("1")
            .withLinePrivateCode("X")
            .build()

        assertEquals(
            "TST_TST-Line-X_1_It-s-a-b-c-d.xml",
            fileName,
        )
    }

    @Test
    fun transliteratesExtendedEuropeanTable() {
        val fileName = fileNameBuilder
            .withCodespace("TST")
            .withLineType("Line")
            .withLineName("ÂáÓÉÊÈéèëÇÜüßªº")
            .withLinePublicCode("1")
            .withLinePrivateCode("X")
            .build()

        assertEquals(
            "TST_TST-Line-X_1_AaOEEEeeeCUuss.xml",
            fileName,
        )
    }

    @Test
    fun removesUnmappedNonAsciiInsteadOfReplacing() {
        val fileName = fileNameBuilder
            .withCodespace("TST")
            .withLineType("Line")
            .withLineName("Linjeα–test")
            .withLinePublicCode("1")
            .withLinePrivateCode("X")
            .build()

        assertEquals(
            "TST_TST-Line-X_1_Linjetest.xml",
            fileName,
        )
    }

    @Test
    fun whitespaceOnlyPublicCodeCountsAsAbsentAndFallsBackToLineId() {
        val fileName = fileNameBuilder
            .withCodespace("TST")
            .withLineType("Line")
            .withLineName("TestLineName")
            .withLinePublicCode("   ")
            .withLineId("AVI:Line:WF")
            .build()

        assertEquals(
            "TST_AVI-Line-WF_TestLineName.xml",
            fileName,
        )
    }

    @Test
    fun trimsSurroundingWhitespaceInPublicCode() {
        val fileName = fileNameBuilder
            .withCodespace("TST")
            .withLineType("Line")
            .withLineName("TestLineName")
            .withLinePublicCode("\n  12\n  ")
            .withLinePrivateCode("X")
            .build()

        assertEquals(
            "TST_TST-Line-X_12_TestLineName.xml",
            fileName,
        )
    }

    @Test
    fun trimsPublicCodeOnlyAndLeavesOtherFieldsUntouched() {
        val fileName = fileNameBuilder
            .withCodespace("TST")
            .withLineType("Line")
            .withLineName("  Bussen  ")
            .withLinePublicCode("1")
            .withLinePrivateCode("X")
            .build()

        assertEquals(
            "TST_TST-Line-X_1_--Bussen--.xml",
            fileName,
        )
    }

    @Test
    fun withLineIdSanitizesOnAssignment() {
        fileNameBuilder.withLineId("AVI:Line:../x")
        assertEquals("AVI-Line----x", fileNameBuilder.lineId)
    }

    @Test
    fun sanitizesPathSeparatorsAndTraversalInLineId() {
        val fileName = fileNameBuilder
            .withCodespace("TST")
            .withLineType("Line")
            .withLineName("TestLineName")
            .withLineId("AVI:Line:../../etc/tst x")
            .build()

        assertEquals(
            "TST_AVI-Line-------etc-tst-x_TestLineName.xml",
            fileName,
        )
    }

    @Test
    fun sanitizesNonAsciiInLineId() {
        val fileName = fileNameBuilder
            .withCodespace("TST")
            .withLineType("Line")
            .withLineName("TestLineName")
            .withLineId("AVI:Line:Ålesund–α")
            .build()

        assertEquals(
            "TST_AVI-Line-Alesund_TestLineName.xml",
            fileName,
        )
    }

    @Test
    fun sharpSExpandsToDoubleS() {
        val fileName = fileNameBuilder
            .withCodespace("TST")
            .withLineType("Line")
            .withLineName("Straße")
            .withLinePublicCode("1")
            .withLinePrivateCode("X")
            .build()

        assertEquals(
            "TST_TST-Line-X_1_Strasse.xml",
            fileName,
        )
    }
}