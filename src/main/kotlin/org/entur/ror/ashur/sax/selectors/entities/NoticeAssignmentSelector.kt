package org.entur.ror.ashur.sax.selectors.entities

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.netex.tools.lib.selections.EntitySelection
import org.entur.netex.tools.lib.selectors.entities.EntitySelector
import org.entur.netex.tools.lib.selectors.entities.EntitySelectorContext

class NoticeAssignmentSelector: EntitySelector {
    fun findNoticeAssignmentsToKeep(entityModel: EntityModel, entitySelection: EntitySelection): List<Entity> {
        val noticeAssignments = entityModel.getEntitiesOfType(NetexTypes.NOTICE_ASSIGNMENT)
        return noticeAssignments.filter { noticeAssignment ->
            val noticeAssignmentId = noticeAssignment.id
            val noticedObjectRef = entityModel.getRefsOfTypeFrom(noticeAssignmentId, "NoticedObjectRef").firstOrNull()?.ref
            entitySelection.includes(noticedObjectRef!!)
        }
    }

    override fun selectEntities(context: EntitySelectorContext): EntitySelection {
        val model = context.entityModel
        val currentEntitySelection = context.currentEntitySelection!!

        val noticeAssignmentsToKeep = findNoticeAssignmentsToKeep(model, currentEntitySelection)
        val noticeAssignmentMap = mutableMapOf<String, Entity>()
        noticeAssignmentsToKeep.forEach { noticeAssignment ->
            noticeAssignmentMap[noticeAssignment.id] = noticeAssignment
        }

        return currentEntitySelection.withReplaced(
            NetexTypes.NOTICE_ASSIGNMENT,
            noticeAssignmentMap
        )
    }
}