package waffle.guam.api.response

import waffle.guam.image.model.Image
import waffle.guam.project.model.Due
import waffle.guam.project.model.Project
import waffle.guam.project.model.ProjectState
import waffle.guam.stack.model.TechStack
import waffle.guam.task.model.Task
import waffle.guam.thread.model.ThreadOverView
import java.time.Instant

data class ProjectResponse(
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
    val leaderProfile: UserResponse?,
    val noticeThread: ThreadOverView?
) {
    companion object {
        fun of(d: Project) = ProjectResponse(
            id = d.id,
            title = d.title,
            description = d.description,
            frontHeadCnt = d.frontHeadCnt,
            backHeadCnt = d.backHeadCnt,
            designHeadCnt = d.designHeadCnt,
            state = d.state,
            due = d.due,
            thumbnail = d.thumbnail,
            techStacks = d.techStacks,
            tasks = d.tasks,
            frontLeftCnt = d.frontLeftCnt,
            backLeftCnt = d.backLeftCnt,
            designLeftCnt = d.designLeftCnt,
            createdAt = d.createdAt,
            modifiedAt = d.modifiedAt,
            leaderProfile = d.leaderProfile?.let { UserResponse.of(it) },
            noticeThread = d.noticeThread
        )
    }
}
