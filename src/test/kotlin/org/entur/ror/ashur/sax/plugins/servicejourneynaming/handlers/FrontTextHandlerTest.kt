package org.entur.ror.ashur.sax.plugins.servicejourneynaming.handlers

import org.entur.netex.tools.lib.model.Entity
import org.entur.ror.ashur.sax.plugins.servicejourneynaming.ServiceJourneyNameParsingContext
import org.entur.ror.ashur.sax.plugins.servicejourneynaming.ServiceJourneyNameRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class FrontTextHandlerTest {

    @Test
    fun `collects FrontText when inside DestinationDisplay`() {
        val repository = ServiceJourneyNameRepository()
        val handler = FrontTextHandler(repository)
        val context = ServiceJourneyNameParsingContext(currentDestinationDisplayId = "DD:1")
        val entity = mock<Entity>()

        val text = "Oslo S"
        handler.characters(context, text.toCharArray(), 0, text.length)
        handler.endElement(context, entity)

        assertEquals("Oslo S", repository.destinationDisplayFrontText["DD:1"])
    }

    @Test
    fun `does not collect FrontText when not inside DestinationDisplay`() {
        val repository = ServiceJourneyNameRepository()
        val handler = FrontTextHandler(repository)
        val context = ServiceJourneyNameParsingContext(currentDestinationDisplayId = null)
        val entity = mock<Entity>()

        val text = "Oslo S"
        handler.characters(context, text.toCharArray(), 0, text.length)
        handler.endElement(context, entity)

        assertNull(repository.destinationDisplayFrontText["DD:1"])
    }

    @Test
    fun `trims whitespace from FrontText`() {
        val repository = ServiceJourneyNameRepository()
        val handler = FrontTextHandler(repository)
        val context = ServiceJourneyNameParsingContext(currentDestinationDisplayId = "DD:1")
        val entity = mock<Entity>()

        val text = "  Oslo S  "
        handler.characters(context, text.toCharArray(), 0, text.length)
        handler.endElement(context, entity)

        assertEquals("Oslo S", repository.destinationDisplayFrontText["DD:1"])
    }

    @Test
    fun `does not store empty FrontText`() {
        val repository = ServiceJourneyNameRepository()
        val handler = FrontTextHandler(repository)
        val context = ServiceJourneyNameParsingContext(currentDestinationDisplayId = "DD:1")
        val entity = mock<Entity>()

        val text = "   "
        handler.characters(context, text.toCharArray(), 0, text.length)
        handler.endElement(context, entity)

        assertNull(repository.destinationDisplayFrontText["DD:1"])
    }
}
