package waffle.guam.project.model

import waffle.guam.image.model.Image
import waffle.guam.project.ProjectEntity
import waffle.guam.stack.model.TechStack
import waffle.guam.task.model.Position
import waffle.guam.task.model.Task
import waffle.guam.task.model.UserState
import waffle.guam.user.model.User
import java.time.Instant

data class Project(
    val id: Long,
    val title: String,
    val description: String,
    val frontHeadCnt: Int,
    val backHeadCnt: Int,
    val designHeadCnt: Int,
    val state: ProjectState,
    val due: Due,
    val thumbnail: Image?,
    val techStacks: List<TechStack>?,
    val tasks: List<Task>?,
    val frontLeftCnt: Int?,
    val backLeftCnt: Int?,
    val designLeftCnt: Int?,
    val createdAt: Instant,
    val modifiedAt: Instant,
    val leaderProfile: User?,
) {
    companion object {

        fun of(
            entity: ProjectEntity,
            techStacks: List<TechStack>,
            tasks: List<Task>,
        ): Project {
            val tasksByPosition = Position.values().associateWith { position ->
                tasks.filter { it.position == position }
            }

            return Project(
                id = entity.id,
                title = entity.title,
                description = entity.description,
                frontHeadCnt = tasksByPosition[Position.FRONTEND]?.size ?: 0,
                backHeadCnt = tasksByPosition[Position.BACKEND]?.size ?: 0,
                designHeadCnt = tasksByPosition[Position.DESIGNER]?.size ?: 0,
                state = ProjectState.valueOf(entity.state),
                due = Due.valueOf(entity.due),
                // thumbnail = TODO("entity.thumbnail?.let { it.toDomain() }"),
                thumbnail = null,
                techStacks = techStacks,
                tasks = tasks.filter { it.user != null },
                frontLeftCnt = tasksByPosition[Position.FRONTEND]?.filter { it.user == null }?.size ?: 0,
                backLeftCnt = tasksByPosition[Position.BACKEND]?.filter { it.user == null }?.size ?: 0,
                designLeftCnt = tasksByPosition[Position.DESIGNER]?.filter { it.user == null }?.size ?: 0,
                createdAt = entity.createdAt,
                modifiedAt = entity.modifiedAt,
                leaderProfile = tasks.singleOrNull { it.userState == UserState.LEADER }?.user
            )
        }
    }
}
