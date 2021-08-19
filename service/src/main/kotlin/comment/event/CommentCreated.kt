package waffle.guam.comment.event

import org.springframework.web.multipart.MultipartFile
import waffle.guam.comment.command.CreateComment
import waffle.guam.thread.ThreadEntity
import waffle.guam.user.UserEntity
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
) : CommentEvent(timestamp) {
    companion object {
        fun of(thread: ThreadEntity, commentId: Long, command: CreateComment, creator: UserEntity): CommentCreated =
            CommentCreated(
                projectId = thread.projectId,
                commentId = commentId,
                threadCreatorId = thread.userId,
                commentCreatorId = command.userId,
                commentCreatorName = creator.nickname,
                content = command.content ?: "이미지 등록",
                imageFiles = command.imageFiles
            )
    }
}
