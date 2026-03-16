package org.entur.ror.ashur.sax.plugins.servicejourneynaming

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ServiceJourneyNameRepositoryTest {

    @Test
    fun `getFrontTextForServiceJourney returns FrontText when chain is complete`() {
        val repo = ServiceJourneyNameRepository()
        repo.destinationDisplayFrontText["DD:1"] = "Oslo S"
        repo.journeyPatternToDestinationDisplay["JP:1"] = "DD:1"
        repo.serviceJourneyToJourneyPattern["SJ:1"] = "JP:1"

        assertEquals("Oslo S", repo.getFrontTextForServiceJourney("SJ:1"))
    }

    @Test
    fun `getFrontTextForServiceJourney returns null when ServiceJourney not found`() {
        val repo = ServiceJourneyNameRepository()

        assertNull(repo.getFrontTextForServiceJourney("SJ:unknown"))
    }

    @Test
    fun `getFrontTextForServiceJourney returns null when JourneyPattern not found`() {
        val repo = ServiceJourneyNameRepository()
        repo.serviceJourneyToJourneyPattern["SJ:1"] = "JP:unknown"

        assertNull(repo.getFrontTextForServiceJourney("SJ:1"))
    }

    @Test
    fun `getFrontTextForServiceJourney returns null when DestinationDisplay not found`() {
        val repo = ServiceJourneyNameRepository()
        repo.journeyPatternToDestinationDisplay["JP:1"] = "DD:unknown"
        repo.serviceJourneyToJourneyPattern["SJ:1"] = "JP:1"

        assertNull(repo.getFrontTextForServiceJourney("SJ:1"))
    }

    @Test
    fun `hasExistingName returns true when ServiceJourney has Name`() {
        val repo = ServiceJourneyNameRepository()
        repo.serviceJourneysWithName.add("SJ:1")

        assertTrue(repo.hasExistingName("SJ:1"))
    }

    @Test
    fun `hasExistingName returns false when ServiceJourney has no Name`() {
        val repo = ServiceJourneyNameRepository()

        assertFalse(repo.hasExistingName("SJ:1"))
    }
}
