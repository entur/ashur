package org.entur.ror.ashur.sax.plugins.filenames

import org.entur.netex.tools.lib.model.NetexTypes
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.xml.sax.helpers.AttributesImpl
import java.io.File
import kotlin.test.assertEquals

class FileNamePluginTest {
    private lateinit var context: FileNamePluginContext
    private lateinit var fileNamePlugin: FileNamePlugin

    private val repo = FileNameRepository()

    @BeforeEach
    fun setUp() {
        context = FileNamePluginContext()
        fileNamePlugin = FileNamePlugin(
            fileNameRepository = repo,
            context = context,
            codespace = "rb_tst",
        )
    }

    @Test
    fun testStartElementForLineElement() {
        val lineAttrs = AttributesImpl()
        lineAttrs.addAttribute("", "id", "id", "CDATA", "line1")
        fileNamePlugin.startElement("Line", lineAttrs, null)
        assertEquals("Line", fileNamePlugin.currentEntityType)
    }

    @Test
    fun testStartElementForFlexibleLineElement() {
        val flexLineAttrs = AttributesImpl()
        flexLineAttrs.addAttribute("", "id", "id", "CDATA", "flexline1")
        fileNamePlugin.startElement("FlexibleLine", flexLineAttrs, null)
        assertEquals("FlexibleLine", fileNamePlugin.currentEntityType)
    }

    @Test
    fun testStartElementForNonLineElement() {
        val journeyAttrs = AttributesImpl()
        journeyAttrs.addAttribute("", "id", "id", "CDATA", "servicejourney1")
        fileNamePlugin.startElement("ServiceJourney", journeyAttrs, null)
        assertEquals( "", context.lineType)
    }

    @Test
    fun characters() {
        val attrs = AttributesImpl()
        attrs.addAttribute("", "id", "id", "CDATA", "line1")
        fileNamePlugin.startElement("Line", attrs, null)

        val nameChars = "TestName".toCharArray()
        fileNamePlugin.characters("Name", nameChars, 0, nameChars.size)
        assertEquals("TestName", context.lineName.toString())

        val publicCodeChars = "PublicCodeTest".toCharArray()
        fileNamePlugin.characters("PublicCode", publicCodeChars, 0, publicCodeChars.size)
        assertEquals("PublicCodeTest", context.linePublicCode.toString())

        val privateCodeChars = "PrivateCodeTest".toCharArray()
        fileNamePlugin.characters("PrivateCode", privateCodeChars, 0, privateCodeChars.size)
        assertEquals("PrivateCodeTest", context.linePrivateCode.toString())
    }

    @Test
    fun endElement() {
        val attrs = AttributesImpl()
        attrs.addAttribute("", "id", "id", "CDATA", "line1")
        fileNamePlugin.startElement("Line", attrs, null)
        assertEquals("Line", fileNamePlugin.currentEntityType)

        fileNamePlugin.endElement("Line", null)
        assertEquals(null, fileNamePlugin.currentEntityType)
    }

    @Test
    fun endDocument() {
        val attrs = AttributesImpl()
        attrs.addAttribute("", "id", "id", "CDATA", "line1")
        fileNamePlugin.startElement("Line", attrs, null)

        val nameChars = "TestName".toCharArray()
        fileNamePlugin.characters("Name", nameChars, 0, nameChars.size)

        val publicCodeChars = "PublicCodeTest".toCharArray()
        fileNamePlugin.characters("PublicCode", publicCodeChars, 0, publicCodeChars.size)

        val privateCodeChars = "PrivateCodeTest".toCharArray()
        fileNamePlugin.characters("PrivateCode", privateCodeChars, 0, privateCodeChars.size)

        fileNamePlugin.endDocument(File("test.xml"))

        val expectedFileName = "TST_TST-Line-PrivateCodeTest_PublicCodeTest_TestName.xml"

        assertEquals(expectedFileName, repo.filesToRename["test.xml"])
        assertEquals("", context.lineType)
        assertEquals("", context.lineName.toString())
        assertEquals("", context.linePublicCode.toString())
        assertEquals("", context.linePrivateCode.toString())
    }

    @Test
    fun getSupportedElementTypes() {
        val supportedTypes = fileNamePlugin.getSupportedElementTypes()
        assert(supportedTypes.containsAll(
            setOf(
                NetexTypes.LINE,
                NetexTypes.FLEXIBLE_LINE,
                NetexTypes.NAME,
                NetexTypes.PUBLIC_CODE,
                NetexTypes.PRIVATE_CODE,
            )
        ))
    }
}