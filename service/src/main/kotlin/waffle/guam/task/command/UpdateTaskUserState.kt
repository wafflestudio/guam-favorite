package waffle.guam.task.command

import waffle.guam.db.entity.UserState

data class UpdateTaskUserState(
    val taskIds: List<Long>,
    val userState: UserState
)
