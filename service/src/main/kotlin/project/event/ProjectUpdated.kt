package waffle.guam.project.event

import org.springframework.web.multipart.MultipartFile
import waffle.guam.projectstack.command.StackIdList
import waffle.guam.task.model.PositionQuota
import java.time.Instant

data class ProjectUpdated(
    val projectId: Long,
    val userId: Long,
    val projectTitle: String,
    val stackIdList: StackIdList,
    val imageFiles: MultipartFile?,
    val positionQuotas: List<PositionQuota>,
    override val timestamp: Instant = Instant.now()
) : ProjectEvent(timestamp)
