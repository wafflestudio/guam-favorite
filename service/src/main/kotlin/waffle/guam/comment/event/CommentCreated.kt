package waffle.guam.comment.event

import org.springframework.web.multipart.MultipartFile

data class CommentCreated(
    val commentId: Long,
    val imageFiles: List<MultipartFile>?,
) : CommentEvent()
