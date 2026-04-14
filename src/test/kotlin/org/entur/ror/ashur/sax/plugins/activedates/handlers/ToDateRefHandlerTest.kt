package org.entur.ror.ashur.sax.plugins.activedates.handlers

import org.entur.netex.tools.lib.extensions.toAttributes
import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.ror.ashur.data.TestDataFactory
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesParsingContext
import org.entur.ror.ashur.sax.plugins.activedates.ActiveDatesRepository
import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

class ToDateRefHandlerTest {
    val repo = ActiveDatesRepository()
    val context = ActiveDatesParsingContext()
    val handler = ToDateRefHandler(repo)

    val operatingPeriodId = "TST:OperatingPeriod:1"
    val toDateId = "TST:ToDate:1"

    val entity = TestDataFactory.defaultEntity(
        id = operatingPeriodId,
        type = NetexTypes.OPERATING_PERIOD
    )

    @Test
    fun handlerCollectsToDateRefForOperatingPeriod() {
        handler.startElement(
            context, mapOf(
                "ref" to toDateId
            ).toAttributes(), entity
        )

        assertEquals(
            toDateId,
            repo.getOperatingPeriodData(operatingPeriodId).toDateId,
        )
    }
}