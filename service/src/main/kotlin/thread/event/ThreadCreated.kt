package waffle.guam.thread.event

import org.springframework.web.multipart.MultipartFile
import java.time.Instant

data class ThreadCreated(
    val projectId: Long,
    val threadId: Long,
    val creatorId: Long,
    val creatorName: String,
    val content: String,
    val imageFiles: List<MultipartFile>?,
    override val timestamp: Instant = Instant.now(),
) : ThreadEvent(timestamp)
