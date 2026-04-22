package org.entur.ror.ashur.sax.plugins.operatorref

import org.entur.ror.ashur.data.TestDataFactory
import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

class OperatorRefPluginTest {

    @Test
    fun `collects OperatorRef from Line entity`() {
        val repository = OperatorRefRepository()
        val plugin = OperatorRefPlugin(repository)
        val entity = TestDataFactory.defaultEntity(id = "TST:Line:1", type = "Line")
        val attributes = TestDataFactory.defaultAttributes(ref = "TST:Operator:1")

        plugin.startElement("OperatorRef", attributes, entity)

        assertEquals("TST:Operator:1", repository.getOperatorRefForLine("TST:Line:1"))
    }

    @Test
    fun `tracks ServiceJourney that has OperatorRef`() {
        val repository = OperatorRefRepository()
        val plugin = OperatorRefPlugin(repository)
        val entity = TestDataFactory.defaultEntity(id = "TST:ServiceJourney:1", type = "ServiceJourney")
        val attributes = TestDataFactory.defaultAttributes(ref = "TST:Operator:1")

        plugin.startElement("OperatorRef", attributes, entity)

        assertTrue(repository.hasOperatorRef("TST:ServiceJourney:1"))
    }

    @Test
    fun `ignores OperatorRef from other entity types`() {
        val repository = OperatorRefRepository()
        val plugin = OperatorRefPlugin(repository)
        val entity = TestDataFactory.defaultEntity(id = "TST:Authority:1", type = "Authority")
        val attributes = TestDataFactory.defaultAttributes(ref = "TST:Operator:1")

        plugin.startElement("OperatorRef", attributes, entity)

        assertTrue(repository.lineOperatorRefs.isEmpty())
        assertFalse(repository.hasOperatorRef("TST:Authority:1"))
    }

    @Test
    fun `ignores OperatorRef without ref attribute`() {
        val repository = OperatorRefRepository()
        val plugin = OperatorRefPlugin(repository)
        val entity = TestDataFactory.defaultEntity(id = "TST:Line:1", type = "Line")
        val attributes = TestDataFactory.defaultAttributes()

        plugin.startElement("OperatorRef", attributes, entity)

        assertNull(repository.getOperatorRefForLine("TST:Line:1"))
    }

    @Test
    fun `ignores OperatorRef when currentEntity is null`() {
        val repository = OperatorRefRepository()
        val plugin = OperatorRefPlugin(repository)
        val attributes = TestDataFactory.defaultAttributes(ref = "TST:Operator:1")

        plugin.startElement("OperatorRef", attributes, null)

        assertTrue(repository.lineOperatorRefs.isEmpty())
    }
}
