package org.entur.ror.ashur.filter

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class IncludeBlocksAndRestrictedJourneysFilteringProfileConfigTest {

    @Test
    fun testDoesNotSkipVehicleScheduleFrame() {
        val filterContext = FilterContext(profile = FilterProfile.IncludeBlocksAndRestrictedJourneysFilter, codespace = "TST")
        val config = IncludeBlocksAndRestrictedJourneysFilteringProfileConfig().build(filterContext)

        assertFalse(config.skipElements.contains("/PublicationDelivery/dataObjects/CompositeFrame/frames/VehicleScheduleFrame"))
        assertFalse(config.skipElements.contains("/PublicationDelivery/dataObjects/VehicleScheduleFrame"))
    }

    @Test
    fun testKeepsPrivateData() {
        val filterContext = FilterContext(profile = FilterProfile.IncludeBlocksAndRestrictedJourneysFilter, codespace = "TST")
        val config = IncludeBlocksAndRestrictedJourneysFilteringProfileConfig().build(filterContext)

        assertFalse(config.removePrivateData)
    }

    @Test
    fun testHasBlockSelector() {
        val filterContext = FilterContext(profile = FilterProfile.IncludeBlocksAndRestrictedJourneysFilter, codespace = "TST")
        val config = IncludeBlocksAndRestrictedJourneysFilteringProfileConfig().build(filterContext)

        assertTrue(config.entitySelectors.any { it.javaClass.simpleName == "BlockSelector" })
    }
}
