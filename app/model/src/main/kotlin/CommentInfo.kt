package waffle.guam.favorite.api.model

data class CommentInfo(
    val postCommentId: Long,
    val count: Long,
    val like: Boolean,
)
