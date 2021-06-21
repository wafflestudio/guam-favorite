package waffle.guam.model

import waffle.guam.db.entity.ProjectView
import java.time.LocalDateTime

data class Project(
    val id: Long,
    val title: String,
    val description: String,
    val thumbnail: String,
    val frontLeftCnt: Int,
    val backLeftCnt: Int,
    val designLeftCnt: Int,
    val isRecruiting: Boolean,
    val techStacks: List<TechStack>,
    val tasks: List<Task>?,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime,
) {
    companion object {
        fun of(entity: ProjectView, fetchTasks: Boolean = false): Project =
            Project(
                id = entity.id,
                title = entity.title,
                description = entity.description,
                thumbnail = entity.thumbnail,
                frontLeftCnt = entity.frontHeadcount,
                backLeftCnt = entity.backHeadcount,
                designLeftCnt = entity.designerHeadcount,
                isRecruiting = entity.recruiting,
                techStacks = entity.techStacks.map { TechStack.of(it.techStack) },
                tasks = when (fetchTasks) {
                    true -> entity.tasks.map { Task.of(it) }
                    else -> null
                },
                createdAt = entity.createdAt,
                modifiedAt = entity.modifiedAt
            )
    }
}
