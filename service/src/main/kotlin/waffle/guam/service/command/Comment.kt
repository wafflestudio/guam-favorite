package waffle.guam.service.command

import waffle.guam.db.entity.CommentEntity

sealed class CommentCommand

data class CreateComment(
    val threadId: Long,
    val userId: Long,
    val content: String
) : CommentCommand() {
    fun toEntity() = CommentEntity(threadId = threadId, userId = userId, content = content)
}

data class EditComment(
    val commentId: Long,
    val userId: Long,
    val content: String
) : CommentCommand()

data class DeleteComment(
    val commentId: Long,
    val userId: Long,
) : CommentCommand()
