package waffle.guam.task.command

import waffle.guam.task.model.UserState

data class UpdateTaskUserState(
    val taskIds: List<Long>,
    val userState: UserState
)
