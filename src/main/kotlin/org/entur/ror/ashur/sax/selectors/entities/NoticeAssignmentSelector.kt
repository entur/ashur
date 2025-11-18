package org.entur.ror.ashur.sax.selectors.entities

import org.entur.netex.tools.lib.model.Entity
import org.entur.netex.tools.lib.model.EntityModel
import org.entur.netex.tools.lib.model.NetexTypes
import org.entur.netex.tools.lib.selections.EntitySelection
import org.entur.netex.tools.lib.selectors.entities.EntitySelector

class NoticeAssignmentSelector: EntitySelector {
    fun findNoticeAssignmentsToKeep(entityModel: EntityModel, entitySelection: EntitySelection): List<Entity> {
        val noticeAssignments = entityModel.getEntitiesOfType(NetexTypes.NOTICE_ASSIGNMENT)
        return noticeAssignments.filter { noticeAssignment ->
            val noticeAssignmentId = noticeAssignment.id
            val noticedObjectRef = entityModel.getRefsOfTypeFrom(noticeAssignmentId, "NoticedObjectRef").firstOrNull()?.ref
            entitySelection.includes(noticedObjectRef!!)
        }
    }

    override fun selectEntities(model: EntityModel, currentEntitySelection: EntitySelection?): EntitySelection {
        val entitySelection: EntitySelection = currentEntitySelection!!
        val noticeAssignmentsToKeep = findNoticeAssignmentsToKeep(model, entitySelection)
        entitySelection.selection[NetexTypes.NOTICE_ASSIGNMENT] = mutableMapOf()
        noticeAssignmentsToKeep.forEach { noticeAssignment ->
            entitySelection.selection[NetexTypes.NOTICE_ASSIGNMENT]!![noticeAssignment.id] = noticeAssignment
        }
        return entitySelection
    }
}