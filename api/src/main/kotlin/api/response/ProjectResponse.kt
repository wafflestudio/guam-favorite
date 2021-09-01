package waffle.guam.api.response

import com.fasterxml.jackson.annotation.JsonFormat
import waffle.guam.image.model.Image
import waffle.guam.project.model.Due
import waffle.guam.project.model.Project
import waffle.guam.project.model.ProjectState
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
    val techStacks: List<TechStackResponse>?,
    val tasks: List<TaskResponse>?,
    val frontLeftCnt: Int?,
    val backLeftCnt: Int?,
    val designLeftCnt: Int?,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "Asia/Seoul")
    val createdAt: Instant,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "Asia/Seoul")
    val modifiedAt: Instant,
    val leaderProfile: UserResponse?,
    val noticeThread: ThreadOverViewResponse?,
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
            techStacks = d.techStacks?.map { TechStackResponse.of(it) },
            tasks = d.tasks?.map { TaskResponse.of(it) },
            frontLeftCnt = d.frontLeftCnt,
            backLeftCnt = d.backLeftCnt,
            designLeftCnt = d.designLeftCnt,
            createdAt = d.createdAt,
            modifiedAt = d.modifiedAt,
            leaderProfile = d.leaderProfile?.let { UserResponse.of(it) },
            noticeThread = d.noticeThread?.let { ThreadOverViewResponse.of(it) }
        )
    }
}
