package waffle.guam.favorite.data.redis.repository

interface UserRepository {
    suspend fun hasPostLike(userId: Long, postId: Long): Boolean
    suspend fun hasPostScrap(userId: Long, postId: Long): Boolean
    suspend fun hasCommentLike(userId: Long, postId: Long): Boolean

    suspend fun getPostLikes(userId: Long): List<Long>
    suspend fun getPostScraps(userId: Long): List<Long>
    suspend fun getCommentLikes(userId: Long): List<Long>
}