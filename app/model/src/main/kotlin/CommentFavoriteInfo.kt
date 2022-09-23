package waffle.guam.favorite.api.model

data class CommentFavoriteInfo(
    val postCommentId: Long,
    val count: Long,
    val like: Boolean,
)
