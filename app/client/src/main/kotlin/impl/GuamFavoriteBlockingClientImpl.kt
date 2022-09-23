package waffle.guam.favorite.client.impl

import kotlinx.coroutines.runBlocking
import org.springframework.web.reactive.function.client.WebClient
import waffle.guam.favorite.api.model.CommentFavoriteInfo
import waffle.guam.favorite.api.model.PostFavoriteInfo
import waffle.guam.favorite.client.GuamFavoriteClient

class GuamFavoriteBlockingClientImpl(
    url: String,
    builder: WebClient.Builder,
    fallback: Boolean,
) : GuamFavoriteClient.Blocking {
    private val client = GuamFavoriteClientImpl(url, builder, fallback)

    override fun getPostInfo(userId: Long, postId: Long): PostFavoriteInfo = runBlocking {
        client.getPostInfo(userId, postId)
    }

    override fun getPostInfos(userId: Long, postIds: List<Long>): Map<Long, PostFavoriteInfo> = runBlocking {
        client.getPostInfos(userId, postIds)
    }

    override fun getCommentInfo(userId: Long, commentId: Long): CommentFavoriteInfo = runBlocking {
        client.getCommentInfo(userId, commentId)
    }

    override fun getCommentInfos(userId: Long, commentIds: List<Long>): Map<Long, CommentFavoriteInfo> = runBlocking {
        client.getCommentInfos(userId, commentIds)
    }

    override fun getRankedPostIds(boardId: Long?, rankFrom: Int, rankTo: Int): List<Long> = runBlocking {
        client.getRankedPostIds(boardId, rankFrom, rankTo)
    }

    override fun getScrappedPostIds(userId: Long, page: Int): List<Long> = runBlocking {
        client.getScrappedPostIds(userId, page)
    }
}
