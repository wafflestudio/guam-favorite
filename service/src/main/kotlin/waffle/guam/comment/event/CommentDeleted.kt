package waffle.guam.comment.event

import waffle.guam.db.entity.CommentEntity

data class CommentDeleted(
    val commentId: Long,
    val targetComment: CommentEntity? // TODO: Shouldn't be nullable
) : CommentEvent()
