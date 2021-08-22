package waffle.guam.task.query

import org.springframework.data.jpa.domain.Specification
import waffle.guam.task.TaskEntity
import waffle.guam.task.TaskSpec
import waffle.guam.task.model.Position
import waffle.guam.task.model.UserState

data class SearchTask private constructor(
    val userIds: List<Long>? = null,
    val projectIds: List<Long>? = null,
    val userStates: List<UserState>? = null,
    val positions: List<Position>? = null,
) {
    val spec: Specification<TaskEntity> =
        TaskSpec.run {
            all().and(userIds?.let { userIds(it) })
                .and(projectIds?.let { projectIds(it) })
                .and(userStates?.let { userStates(it.map { it.name }) })
                .and(positions?.let { positions(it.map { it.name }) })
        }

    fun specWithFetch(extraFieldParams: TaskExtraFieldParams): Specification<TaskEntity> =
        spec.and(extraFieldParams.spec)

    fun userIds(vararg value: Long): SearchTask =
        copy(userIds = value.toList())

    fun projectIds(vararg value: Long): SearchTask =
        copy(projectIds = value.toList())

    fun projectIds(value: List<Long>): SearchTask =
        copy(projectIds = value)

    fun userStates(vararg value: UserState): SearchTask =
        copy(userStates = value.toList())

    fun userStates(value: List<UserState>): SearchTask =
        copy(userStates = value)

    fun positions(vararg value: Position): SearchTask =
        copy(positions = value.toList())

    fun positions(value: List<Position>): SearchTask =
        copy(positions = value)

    companion object {
        fun taskQuery(): SearchTask = SearchTask()
    }
}
