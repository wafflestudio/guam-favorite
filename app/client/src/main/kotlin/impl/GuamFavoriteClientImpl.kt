package waffle.guam.favorite.client.impl

import org.slf4j.LoggerFactory
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import waffle.guam.favorite.api.model.CommentInfo
import waffle.guam.favorite.api.model.PostInfo
import waffle.guam.favorite.api.model.SuccessResponse
import waffle.guam.favorite.client.GuamFavoriteClient
import java.util.Optional

internal class GuamFavoriteClientImpl(
    url: String,
    builder: WebClient.Builder,
    private val fallback: Boolean,
) : GuamFavoriteClient {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val client = builder.baseUrl(url).build()

    override suspend fun getPostInfo(userId: Long, postId: Long): PostInfo =
        getPostInfos(userId, listOf(postId)).first()

    override suspend fun getPostInfos(userId: Long, postIds: List<Long>): List<PostInfo> = runCatching {
        client.get()
            .uri("/api/v1/views/posts?postIds={postId}&userId={userId}", postIds.joinToString(","), userId)
            .accept()
            .retrieve()
            .awaitBody<SuccessResponse<List<PostInfo>>>()
            .data
    }.getOrElse {
        if (fallback) {
            logger.error("[Favorite][getPostInfos] response unavailable", it)
            postIds.map(this::fallbackPostInfo)
        } else {
            throw it
        }
    }

    override suspend fun getCommentInfo(userId: Long, commentId: Long): CommentInfo =
        getCommentInfos(userId, listOf(commentId)).first()

    override suspend fun getCommentInfos(userId: Long, commentIds: List<Long>): List<CommentInfo> = runCatching {
        client.get()
            .uri(
                "/api/v1/views/comments?postCommentIds={commentId}&userId={userId}",
                commentIds.joinToString(","),
                userId
            )
            .accept()
            .retrieve()
            .awaitBody<SuccessResponse<List<CommentInfo>>>()
            .data
    }.getOrElse {
        if (fallback) {
            logger.error("[Favorite][getCommentInfos] response unavailable", it)
            commentIds.map(this::fallbackCommentInfo)
        } else {
            throw it
        }
    }

    override suspend fun getRankedPostIds(boardId: Long?, rankFrom: Int, rankTo: Int): List<Long> = runCatching {
        client.get()
            .uri {
                it.path("/api/v1/views/posts/rank")
                    .queryParamIfPresent("boardId", Optional.ofNullable(boardId))
                    .queryParam("from", rankFrom)
                    .queryParam("to", rankTo)
                    .build()
            }
            .accept()
            .retrieve()
            .awaitBody<SuccessResponse<List<Long>>>()
            .data
    }.getOrElse {
        if (fallback) {
            logger.error("[Favorite][getRankedPostIds] response unavailable", it)
            emptyList()
        } else {
            throw it
        }
    }

    override suspend fun getScrappedPostIds(userId: Long, page: Int): List<Long> = runCatching {
        client.get()
            .uri("/api/v1/views/users/scrap?userId=$userId&page=$page")
            .accept().retrieve()
            .awaitBody<SuccessResponse<List<Long>>>()
            .data
            .sortedDescending()
    }.getOrElse {
        if (fallback) {
            logger.error("[Favorite][getScrappedPostIds] response unavailable", it)
            emptyList()
        } else {
            throw it
        }
    }

    private fun fallbackPostInfo(postId: Long) = PostInfo(
        postId = postId,
        likeCnt = 0,
        scrapCnt = 0,
        like = false,
        scrap = false
    )

    private fun fallbackCommentInfo(commentId: Long) = CommentInfo(
        postCommentId = commentId,
        count = 0,
        like = false
    )
}
