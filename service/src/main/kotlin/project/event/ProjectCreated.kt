package waffle.guam.project.event

import org.springframework.web.multipart.MultipartFile
import waffle.guam.projectstack.command.StackIdList
import waffle.guam.task.model.Position
import java.time.Instant

class ProjectCreated(
    val projectId: Long,
    val projectTitle: String,
    val stackIdList: StackIdList,
    val leaderId: Long,
    val leaderPosition: Position,
    val imageFiles: MultipartFile?,
    override val timestamp: Instant = Instant.now()
) : ProjectEvent(timestamp)
