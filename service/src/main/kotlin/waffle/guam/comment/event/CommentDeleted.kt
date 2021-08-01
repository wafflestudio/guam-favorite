package waffle.guam.comment.event

import waffle.guam.db.entity.CommentEntity

data class CommentDeleted(
    val commentId: Long,
    val threadId: Long,
) : CommentEvent()
