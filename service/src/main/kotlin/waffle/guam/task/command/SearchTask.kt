package waffle.guam.task.command

import org.springframework.data.jpa.domain.Specification
import waffle.guam.db.entity.Position
import waffle.guam.db.entity.TaskView
import waffle.guam.db.entity.UserEntity
import waffle.guam.db.entity.UserState
import javax.persistence.criteria.CriteriaBuilder

data class SearchTask(
    val userId: Long? = null,
    val projectId: Long? = null,
    val userStates: List<UserState> = emptyList(),
    val positions: List<Position> = emptyList(),
) {
    fun toSpec(): Specification<TaskView> =
        Spec.run {
            all()
                .and(
                    userId?.let { userId(userId) }
                )
                .and(
                    projectId?.let { projectId(projectId) }
                )
                .and(
                    if (userStates.isEmpty()) null
                    else userStates(userStates)
                )
                .and(
                    if (positions.isEmpty()) null
                    else positions(positions)
                )
        }

    private object Spec {
        fun all(): Specification<TaskView> = Specification { _, _, builder: CriteriaBuilder ->
            builder.conjunction()
        }

        fun userId(userId: Long): Specification<TaskView> = Specification { root, _, builder ->
            builder.equal(root.get<UserEntity>("user").get<Long>("id"), userId)
        }

        fun projectId(projectId: Long): Specification<TaskView> = Specification { root, _, builder ->
            builder.equal(root.get<Long>("projectId"), projectId)
        }

        fun userStates(userStates: List<UserState>): Specification<TaskView> = Specification { root, _, _ ->
            root.get<UserState>("userState").`in`(userStates)
        }

        fun positions(positions: List<Position>): Specification<TaskView> = Specification { root, _, _ ->
            root.get<UserState>("positions").`in`(positions)
        }
    }
}
