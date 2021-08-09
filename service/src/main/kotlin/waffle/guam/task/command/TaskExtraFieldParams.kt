package waffle.guam.task.command

import org.springframework.data.jpa.domain.Specification
import waffle.guam.task.db.TaskEntity
import waffle.guam.taskmessage.db.TaskMessageEntity
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.JoinType

data class TaskExtraFieldParams(
    val withTaskMsgs: Boolean = false
) {

    fun toSpec(): Specification<TaskEntity> =
        Spec.all().let {
            if (withTaskMsgs) it.and(Spec.fetchTaskMessages())
            else it
        }

    private object Spec {
        fun all(): Specification<TaskEntity> = Specification { _, _, builder: CriteriaBuilder ->
            builder.conjunction()
        }

        fun fetchTaskMessages(): Specification<TaskEntity> = Specification { root, query, builder: CriteriaBuilder ->
            root.fetch<TaskEntity, Set<TaskMessageEntity>>("taskMessages", JoinType.LEFT)
            query.distinct(true)
            builder.conjunction()
        }
    }
}
