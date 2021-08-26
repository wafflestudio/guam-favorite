package waffle.guam.user.model

import waffle.guam.image.model.Image.Companion.toDomain
import waffle.guam.task.TaskEntity
import waffle.guam.task.TaskHistoryEntity
import waffle.guam.task.model.Position
import waffle.guam.task.model.UserState

data class UserProject(
    val projectId: Long,
    val projectTitle: String,
    val projectThumbnail: String?,
    val position: Position,
    val userState: UserState,
) {
    companion object {
        fun of(e: TaskEntity) = UserProject(
            projectId = e.project.id,
            projectTitle = e.project.title,
            projectThumbnail = e.project.thumbnail?.toDomain()?.path,
            position = Position.valueOf(e.position),
            userState = UserState.valueOf(e.userState!!)
        )

        fun of(e: TaskHistoryEntity) = UserProject(
            projectId = e.project.id,
            projectTitle = e.project.title,
            projectThumbnail = e.project.thumbnail?.toDomain()?.path,
            position = Position.valueOf(e.position),
            userState = UserState.valueOf(e.userState)
        )
    }
}
