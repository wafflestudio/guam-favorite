package waffle.guam.thread.event

import org.springframework.web.multipart.MultipartFile
import waffle.guam.project.ProjectEntity
import waffle.guam.task.model.Task
import waffle.guam.thread.command.CreateThread
import java.time.Instant

data class ThreadCreated(
    val projectId: Long,
    val threadId: Long,
    val creatorId: Long,
    val creatorName: String,
    val content: String,
    val imageFiles: List<MultipartFile>?,
    override val timestamp: Instant = Instant.now(),
) : ThreadEvent(timestamp) {
    companion object {
        fun of(project: ProjectEntity, threadId: Long, command: CreateThread, task: Task): ThreadCreated =
            ThreadCreated(
                projectId = project.id,
                threadId = threadId,
                creatorId = command.userId,
                creatorName = task.user!!.nickname,
                content = command.content ?: "이미지 등록",
                imageFiles = command.imageFiles,
            )
    }
}
