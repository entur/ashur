package org.entur.ror.ashur.sax.plugins.journeypatternname

import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

class JourneyPatternNameRepositoryTest {

    @Test
    fun `getNameForJourneyPattern returns null when JourneyPattern already has name`() {
        val repo = JourneyPatternNameRepository()
        repo.routeNames["TST:Route:1"] = "Route Name"
        repo.journeyPatternRouteRefs["TST:JourneyPattern:1"] = "TST:Route:1"
        repo.journeyPatternsWithName.add("TST:JourneyPattern:1")

        val result = repo.getNameForJourneyPattern("TST:JourneyPattern:1")

        assertNull(result)
    }

    @Test
    fun `getNameForJourneyPattern returns Route name when JourneyPattern has no name`() {
        val repo = JourneyPatternNameRepository()
        repo.routeNames["TST:Route:1"] = "Route Name"
        repo.journeyPatternRouteRefs["TST:JourneyPattern:1"] = "TST:Route:1"

        val result = repo.getNameForJourneyPattern("TST:JourneyPattern:1")

        assertEquals("Route Name", result)
    }

    @Test
    fun `getNameForJourneyPattern returns null when Route has no name`() {
        val repo = JourneyPatternNameRepository()
        repo.journeyPatternRouteRefs["TST:JourneyPattern:1"] = "TST:Route:1"
        // Route name is not set

        val result = repo.getNameForJourneyPattern("TST:JourneyPattern:1")

        assertNull(result)
    }

    @Test
    fun `getNameForJourneyPattern returns null when JourneyPattern has no RouteRef`() {
        val repo = JourneyPatternNameRepository()
        repo.routeNames["TST:Route:1"] = "Route Name"
        // RouteRef mapping is not set

        val result = repo.getNameForJourneyPattern("TST:JourneyPattern:1")

        assertNull(result)
    }

    @Test
    fun `getNameForJourneyPattern returns null for unknown JourneyPattern`() {
        val repo = JourneyPatternNameRepository()

        val result = repo.getNameForJourneyPattern("TST:JourneyPattern:unknown")

        assertNull(result)
    }
}
