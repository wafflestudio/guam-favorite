package waffle.guam.favorite.api.model

data class PostFavoriteInfo(
    val postId: Long,
    val likeCnt: Long,
    val scrapCnt: Long,
    val like: Boolean,
    val scrap: Boolean,
)
