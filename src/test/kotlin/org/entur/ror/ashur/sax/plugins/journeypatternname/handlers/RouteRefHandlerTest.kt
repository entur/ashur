package org.entur.ror.ashur.sax.plugins.journeypatternname.handlers

import org.entur.ror.ashur.data.TestDataFactory
import org.entur.ror.ashur.sax.plugins.journeypatternname.JourneyPatternNameRepository
import org.junit.jupiter.api.Assertions.*
import org.xml.sax.helpers.AttributesImpl
import kotlin.test.Test

class RouteRefHandlerTest {
    private val repo = JourneyPatternNameRepository()
    private val handler = RouteRefHandler(repo)

    private fun attrsWithRef(ref: String): AttributesImpl {
        val attrs = AttributesImpl()
        attrs.addAttribute("", "ref", "ref", "CDATA", ref)
        return attrs
    }

    @Test
    fun `collects RouteRef when inside JourneyPattern`() {
        val journeyPatternEntity = TestDataFactory.defaultEntity(
            id = "TST:JourneyPattern:1",
            type = "JourneyPattern"
        )

        handler.startElement(attrsWithRef("TST:Route:1"), journeyPatternEntity)

        assertEquals("TST:Route:1", repo.journeyPatternRouteRefs["TST:JourneyPattern:1"])
    }

    @Test
    fun `does not collect RouteRef when not inside JourneyPattern`() {
        val otherEntity = TestDataFactory.defaultEntity(
            id = "TST:Other:1",
            type = "SomeOtherType"
        )

        handler.startElement(attrsWithRef("TST:Route:1"), otherEntity)

        assertNull(repo.journeyPatternRouteRefs["TST:Other:1"])
    }

    @Test
    fun `does not throw when ref attribute is missing`() {
        val journeyPatternEntity = TestDataFactory.defaultEntity(
            id = "TST:JourneyPattern:1",
            type = "JourneyPattern"
        )

        assertDoesNotThrow {
            handler.startElement(AttributesImpl(), journeyPatternEntity)
        }
        assertNull(repo.journeyPatternRouteRefs["TST:JourneyPattern:1"])
    }

    @Test
    fun `does not throw when attributes is null`() {
        val journeyPatternEntity = TestDataFactory.defaultEntity(
            id = "TST:JourneyPattern:1",
            type = "JourneyPattern"
        )

        assertDoesNotThrow {
            handler.startElement(null, journeyPatternEntity)
        }
        assertNull(repo.journeyPatternRouteRefs["TST:JourneyPattern:1"])
    }
}
