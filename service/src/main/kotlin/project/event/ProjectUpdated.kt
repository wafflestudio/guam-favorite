package waffle.guam.project.event

import org.springframework.web.multipart.MultipartFile
import waffle.guam.projectstack.command.StackIdList
import java.time.Instant

class ProjectUpdated(
    val projectId: Long,
    val projectTitle: String,
    val stackIdList: StackIdList,
    val imageFiles: MultipartFile?,
    override val timestamp: Instant = Instant.now()
) : ProjectEvent(timestamp)
