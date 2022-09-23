package waffle.guam.favorite.client

import waffle.guam.favorite.api.model.CommentFavoriteInfo
import waffle.guam.favorite.api.model.PostFavoriteInfo

interface GuamFavoriteClient {
    suspend fun getPostInfo(userId: Long, postId: Long): PostFavoriteInfo
    suspend fun getPostInfos(userId: Long, postIds: List<Long>): Map<Long, PostFavoriteInfo>
    suspend fun getCommentInfo(userId: Long, commentId: Long): CommentFavoriteInfo
    suspend fun getCommentInfos(userId: Long, commentIds: List<Long>): Map<Long, CommentFavoriteInfo>
    suspend fun getRankedPostIds(boardId: Long?, rankFrom: Int, rankTo: Int): List<Long>
    suspend fun getScrappedPostIds(userId: Long, page: Int): List<Long>

    interface Blocking {
        fun getPostInfo(userId: Long, postId: Long): PostFavoriteInfo
        fun getPostInfos(userId: Long, postIds: List<Long>): Map<Long, PostFavoriteInfo>
        fun getCommentInfo(userId: Long, commentId: Long): CommentFavoriteInfo
        fun getCommentInfos(userId: Long, commentIds: List<Long>): Map<Long, CommentFavoriteInfo>
        fun getRankedPostIds(boardId: Long?, rankFrom: Int, rankTo: Int): List<Long>
        fun getScrappedPostIds(userId: Long, page: Int): List<Long>
    }
}
