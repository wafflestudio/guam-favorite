package waffle.guam.task.command

import org.springframework.data.jpa.domain.Specification
import waffle.guam.db.entity.Position
import waffle.guam.db.entity.UserState
import waffle.guam.task.db.TaskEntity
import waffle.guam.user.db.UserEntity
import javax.persistence.criteria.CriteriaBuilder

data class SearchTask(
    val userIds: List<Long>? = null,
    val projectIds: List<Long>? = null,
    val userStates: List<UserState>? = null,
    val positions: List<Position>? = null,
) : TaskCommand {
    fun toSpec(): Specification<TaskEntity> =
        Spec.run {
            all().and(userIds?.let { userIds(it) })
                .and(projectIds?.let { projectIds(it) })
                .and(userStates?.let { userStates(it) })
                .and(positions?.let { positions(it) })
        }

    private object Spec {
        fun all(): Specification<TaskEntity> = Specification { _, _, builder: CriteriaBuilder ->
            builder.conjunction()
        }

        fun userIds(userIds: List<Long>): Specification<TaskEntity> = Specification { root, _, builder ->
            root.get<UserEntity>("user").get<Long>("id").`in`(userIds)
        }

        fun projectIds(projectIds: List<Long>): Specification<TaskEntity> = Specification { root, _, builder ->
            root.get<Long>("projectId").`in`(projectIds)
        }

        fun userStates(userStates: List<UserState>): Specification<TaskEntity> = Specification { root, _, _ ->
            root.get<UserState>("userState").`in`(userStates)
        }

        fun positions(positions: List<Position>): Specification<TaskEntity> = Specification { root, _, _ ->
            root.get<UserState>("positions").`in`(positions)
        }
    }
}
