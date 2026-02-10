package org.entur.ror.ashur.filter

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class StandardImportFilteringProfileConfigTest {

    @Test
    fun testSkipsVehicleScheduleFrame() {
        val filterContext = FilterContext(profile = FilterProfile.StandardImportFilter, codespace = "TST")
        val config = StandardImportFilteringProfileConfig().build(filterContext)

        assertTrue(
            config.skipElements.containsAll(
                listOf(
                    "/PublicationDelivery/dataObjects/CompositeFrame/frames/VehicleScheduleFrame",
                    "/PublicationDelivery/dataObjects/VehicleScheduleFrame"
                )
            )
        )
    }

    @Test
    fun testRemovesPrivateData() {
        val filterContext = FilterContext(profile = FilterProfile.StandardImportFilter, codespace = "TST")
        val config = StandardImportFilteringProfileConfig().build(filterContext)

        assertTrue(config.removePrivateData)
    }
}
