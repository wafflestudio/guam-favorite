package waffle.guam.task.command

import waffle.guam.task.db.UserState

data class UpdateTaskUserState(
    val taskIds: List<Long>,
    val userState: UserState
)
