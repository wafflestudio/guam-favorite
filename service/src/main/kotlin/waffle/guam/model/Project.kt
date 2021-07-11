package waffle.guam.model

import waffle.guam.db.entity.Due
import waffle.guam.db.entity.ProjectView
import java.time.LocalDateTime

data class Project(
    val id: Long,
    val title: String,
    val description: String,
    val thumbnail: String?,
    val frontHeadCnt: Int,
    val backHeadCnt: Int,
    val designHeadCnt: Int,
    val frontLeftCnt: Int,
    val backLeftCnt: Int,
    val designLeftCnt: Int,
    val isRecruiting: Boolean,
    val techStacks: List<TechStack>,
    val tasks: List<Task>?,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime,
    val due: Due
) {
    companion object {
        fun of(
            entity: ProjectView,
            fetchTasks: Boolean = false,
            thread: ThreadOverView? = null,
            currHeadCnt: IntArray = IntArray(3, fun(_: Int) = 0)
        ): Project =
            Project(
                id = entity.id,
                title = entity.title,
                description = entity.description,
                thumbnail = entity.thumbnail,
                frontHeadCnt = entity.frontHeadcount,
                backHeadCnt = entity.backHeadcount,
                designHeadCnt = entity.designerHeadcount,
                frontLeftCnt = entity.frontHeadcount - currHeadCnt[0],
                backLeftCnt = entity.backHeadcount - currHeadCnt[1],
                designLeftCnt = entity.designerHeadcount - currHeadCnt[2],
                isRecruiting = entity.recruiting,
                techStacks = entity.techStacks.map { TechStack.of(it.techStack) },
                tasks = when (fetchTasks) {
                    true -> entity.tasks.map { Task.of(it) }
                    else -> null
                },
                createdAt = entity.createdAt,
                modifiedAt = entity.modifiedAt,
                due = entity.due
            )
    }
}
