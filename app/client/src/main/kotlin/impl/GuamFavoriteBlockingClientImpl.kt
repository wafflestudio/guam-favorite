package waffle.guam.favorite.client.impl

import kotlinx.coroutines.runBlocking
import org.springframework.web.reactive.function.client.WebClient
import waffle.guam.favorite.api.model.CommentInfo
import waffle.guam.favorite.api.model.PostInfo
import waffle.guam.favorite.client.GuamFavoriteClient

class GuamFavoriteBlockingClientImpl(
    url: String,
    builder: WebClient.Builder,
    fallback: Boolean,
) : GuamFavoriteClient.Blocking {
    private val client = GuamFavoriteClientImpl(url, builder, fallback)

    override fun getPostInfo(userId: Long, postId: Long): PostInfo = runBlocking {
        client.getPostInfo(userId, postId)
    }

    override fun getPostInfos(userId: Long, postIds: List<Long>): List<PostInfo> = runBlocking {
        client.getPostInfos(userId, postIds)
    }

    override fun getCommentInfo(userId: Long, commentId: Long): CommentInfo = runBlocking {
        client.getCommentInfo(userId, commentId)
    }

    override fun getCommentInfos(userId: Long, commentIds: List<Long>): List<CommentInfo> = runBlocking {
        client.getCommentInfos(userId, commentIds)
    }

    override fun getRankedPostIds(boardId: Long?, rankFrom: Int, rankTo: Int): List<Long> = runBlocking {
        client.getRankedPostIds(boardId, rankFrom, rankTo)
    }

    override fun getScrappedPostIds(userId: Long, page: Int): List<Long> = runBlocking {
        client.getScrappedPostIds(userId, page)
    }
}
