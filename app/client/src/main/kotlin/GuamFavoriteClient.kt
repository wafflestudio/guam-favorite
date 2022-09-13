package waffle.guam.favorite.client

import waffle.guam.favorite.api.model.CommentInfo
import waffle.guam.favorite.api.model.PostInfo

interface GuamFavoriteClient {
    suspend fun getPostInfo(userId: Long, postId: Long): PostInfo
    suspend fun getPostInfos(userId: Long, postIds: List<Long>): List<PostInfo>
    suspend fun getCommentInfo(userId: Long, commentId: Long): CommentInfo
    suspend fun getCommentInfos(userId: Long, commentIds: List<Long>): List<CommentInfo>
    suspend fun getRankedPostIds(boardId: Long?, rankFrom: Int, rankTo: Int): List<Long>
    suspend fun getScrappedPostIds(userId: Long, page: Int): List<Long>

    interface Blocking {
        fun getPostInfo(userId: Long, postId: Long): PostInfo
        fun getPostInfos(userId: Long, postIds: List<Long>): List<PostInfo>
        fun getCommentInfo(userId: Long, commentId: Long): CommentInfo
        fun getCommentInfos(userId: Long, commentIds: List<Long>): List<CommentInfo>
        fun getRankedPostIds(boardId: Long?, rankFrom: Int, rankTo: Int): List<Long>
        fun getScrappedPostIds(userId: Long, page: Int): List<Long>
    }
}
