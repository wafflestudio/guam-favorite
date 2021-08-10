package waffle.guam.task.command

import org.springframework.data.jpa.domain.Specification
import waffle.guam.task.TaskEntity
import waffle.guam.task.TaskSpec
import waffle.guam.task.model.Position
import waffle.guam.task.model.UserState

data class SearchTask(
    val userIds: List<Long>? = null,
    val projectIds: List<Long>? = null,
    val userStates: List<UserState>? = null,
    val positions: List<Position>? = null,
) : TaskCommand {
    val spec: Specification<TaskEntity> =
        TaskSpec.run {
            all().and(userIds?.let { userIds(it) })
                .and(projectIds?.let { projectIds(it) })
                .and(userStates?.let { userStates(it.map { it.name }) })
                .and(positions?.let { positions(it.map { it.name }) })
        }

    fun specWithFetch(extraFieldParams: TaskExtraFieldParams): Specification<TaskEntity> =
        spec.and(extraFieldParams.spec)
}
