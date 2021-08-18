package waffle.guam.comment.event

import org.springframework.web.multipart.MultipartFile
import java.time.Instant

data class CommentCreated(
    val projectId: Long,
    val commentId: Long,
    val threadCreatorId: Long,
    val commentCreatorId: Long,
    val commentCreatorName: String,
    val content: String,
    val imageFiles: List<MultipartFile>?,
    override val timestamp: Instant = Instant.now()
) : CommentEvent(timestamp)
