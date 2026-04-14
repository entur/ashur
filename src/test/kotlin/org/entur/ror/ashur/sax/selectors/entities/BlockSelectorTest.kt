package org.entur.ror.ashur.sax.selectors.entities

import org.entur.netex.tools.lib.model.Ref
import org.entur.netex.tools.lib.selectors.entities.EntitySelectorContext
import org.entur.ror.ashur.data.TestDataFactory
import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

class BlockSelectorTest {

    val selector = BlockSelector()

    @Test
    fun `entitySelection should only contain Block entities that refer to a journey and dayType that are also included in the entitySelection`() {
        val serviceJourney = TestDataFactory.defaultEntity("SJ:1", "ServiceJourney")
        val dayType = TestDataFactory.defaultEntity("DT:1", "DayType")

        val block1 = TestDataFactory.defaultEntity("B:1", "Block")
        val block2 = TestDataFactory.defaultEntity("B:2", "Block")
        val block3 = TestDataFactory.defaultEntity("B:3", "Block")
        val block4 = TestDataFactory.defaultEntity("B:4", "Block")

        val entityModel = TestDataFactory.defaultEntityModel().also {
            it.addEntity(serviceJourney)
            it.addEntity(block1)
            it.addEntity(block2)

            it.addRef(Ref("VehicleJourneyRef", block1, serviceJourney.id))
            it.addRef(Ref("DayTypeRef", block1, dayType.id))

            it.addRef(Ref("DayTypeRef", block2, dayType.id))

            it.addRef(Ref("VehicleJourneyRef", block3, serviceJourney.id))
        }


        val context = EntitySelectorContext(
            entityModel = entityModel,
            currentEntitySelection = TestDataFactory.entitySelection(
                entities = setOf(
                    dayType,
                    serviceJourney,
                    block1,
                    block2,
                )
            )
        )

        val selection = selector.selectEntities(context)
        selector.selectEntities(context)

        assertTrue(selection.isSelected(block1))
        assertFalse(selection.isSelected(block2))
        assertFalse(selection.isSelected(block3))
        assertFalse(selection.isSelected(block4))
    }

}