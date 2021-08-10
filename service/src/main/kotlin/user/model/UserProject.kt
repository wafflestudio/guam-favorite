package waffle.guam.user.model

import waffle.guam.task.model.Position
import waffle.guam.task.model.UserState

data class UserProject(
    val projectId: Long,
    val projectTitle: String,
    val projectThumbnail: String?,
    val position: Position,
    val userState: UserState
)
