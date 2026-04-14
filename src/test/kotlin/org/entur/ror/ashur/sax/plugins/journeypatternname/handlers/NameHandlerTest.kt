package org.entur.ror.ashur.sax.plugins.journeypatternname.handlers

import org.entur.ror.ashur.data.TestDataFactory
import org.entur.ror.ashur.sax.plugins.journeypatternname.JourneyPatternNameRepository
import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

class NameHandlerTest {
    private val repo = JourneyPatternNameRepository()
    private val handler = NameHandler(repo)

    @Test
    fun `marks JourneyPattern as having name when Name element is inside JourneyPattern`() {
        val journeyPatternEntity = TestDataFactory.defaultEntity(
            id = "TST:JourneyPattern:1",
            type = "JourneyPattern"
        )

        handler.startElement(null, journeyPatternEntity)

        assertTrue(repo.journeyPatternsWithName.contains("TST:JourneyPattern:1"))
    }

    @Test
    fun `does not mark as having name when Name element is inside Route`() {
        val routeEntity = TestDataFactory.defaultEntity(
            id = "TST:Route:1",
            type = "Route"
        )

        handler.startElement(null, routeEntity)

        assertFalse(repo.journeyPatternsWithName.contains("TST:Route:1"))
    }

    @Test
    fun `collects Route name from Name element inside Route`() {
        val routeEntity = TestDataFactory.defaultEntity(
            id = "TST:Route:1",
            type = "Route"
        )

        handler.startElement(null, routeEntity)
        handler.characters("My Route Name".toCharArray(), 0, "My Route Name".length)
        handler.endElement(routeEntity)

        assertEquals("My Route Name", repo.routeNames["TST:Route:1"])
    }

    @Test
    fun `does not store empty Route name`() {
        val routeEntity = TestDataFactory.defaultEntity(
            id = "TST:Route:1",
            type = "Route"
        )

        handler.startElement(null, routeEntity)
        handler.characters("   ".toCharArray(), 0, 3)
        handler.endElement(routeEntity)

        assertNull(repo.routeNames["TST:Route:1"])
    }

    @Test
    fun `does not store Route name when Name is inside JourneyPattern`() {
        val journeyPatternEntity = TestDataFactory.defaultEntity(
            id = "TST:JourneyPattern:1",
            type = "JourneyPattern"
        )

        handler.startElement(null, journeyPatternEntity)
        handler.characters("JP Name".toCharArray(), 0, "JP Name".length)
        handler.endElement(journeyPatternEntity)

        assertNull(repo.routeNames["TST:JourneyPattern:1"])
    }

    @Test
    fun `handles multiple character callbacks for Route name`() {
        val routeEntity = TestDataFactory.defaultEntity(
            id = "TST:Route:1",
            type = "Route"
        )

        handler.startElement(null, routeEntity)
        handler.characters("My ".toCharArray(), 0, 3)
        handler.characters("Route ".toCharArray(), 0, 6)
        handler.characters("Name".toCharArray(), 0, 4)
        handler.endElement(routeEntity)

        assertEquals("My Route Name", repo.routeNames["TST:Route:1"])
    }
}
