package waffle.guam.favorite.client.impl

import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import waffle.guam.favorite.api.model.CommentFavoriteInfo
import waffle.guam.favorite.api.model.PostFavoriteInfo
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

    override suspend fun getPostInfo(userId: Long, postId: Long): PostFavoriteInfo =
        getPostInfos(userId, listOf(postId))[postId] ?: run {
            if (fallback) {
                fallbackPostInfo(postId)
            } else {
                throw IllegalArgumentException("PostInfo of $postId is missing.")
            }
        }

    override suspend fun getPostInfos(userId: Long, postIds: List<Long>): Map<Long, PostFavoriteInfo> = runCatching {
        client.get()
            .uri("/api/v1/views/posts?postIds={postId}&userId={userId}", postIds.joinToString(","), userId)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .awaitBody<SuccessResponse<List<PostFavoriteInfo>>>()
            .data
            .let { infos ->
                val resMap = infos.associateBy { it.postId }

                postIds.associateWith {
                    resMap[it] ?: run {
                        if (fallback) {
                            fallbackPostInfo(it)
                        } else {
                            throw IllegalArgumentException("PostInfo of $it is missing.")
                        }
                    }
                }
            }
    }.getOrElse {
        if (fallback) {
            logger.error("[Favorite][getPostInfos] response unavailable", it)
            postIds.associateWith { fallbackPostInfo(it) }
        } else {
            throw it
        }
    }

    override suspend fun getCommentInfo(userId: Long, commentId: Long): CommentFavoriteInfo =
        getCommentInfos(userId, listOf(commentId))[commentId] ?: run {
            if (fallback) {
                fallbackCommentInfo(commentId)
            } else {
                throw IllegalArgumentException("CommentInfo of $commentId is missing.")
            }
        }

    override suspend fun getCommentInfos(userId: Long, commentIds: List<Long>): Map<Long, CommentFavoriteInfo> =
        runCatching {
            client.get()
                .uri(
                    "/api/v1/views/comments?postCommentIds={commentId}&userId={userId}",
                    commentIds.joinToString(","),
                    userId
                )
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .awaitBody<SuccessResponse<List<CommentFavoriteInfo>>>()
                .data
                .let { infos ->
                    val resMap = infos.associateBy { it.postCommentId }

                    commentIds.associateWith {
                        resMap[it] ?: run {
                            if (fallback) {
                                fallbackCommentInfo(it)
                            } else {
                                throw IllegalArgumentException("CommentInfo of $it is missing.")
                            }
                        }
                    }
                }
        }.getOrElse {
            if (fallback) {
                logger.error("[Favorite][getCommentInfos] response unavailable", it)
                commentIds.associateWith { fallbackCommentInfo(it) }
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
            .accept(MediaType.APPLICATION_JSON)
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
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
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

    private fun fallbackPostInfo(postId: Long) = PostFavoriteInfo(
        postId = postId,
        likeCnt = 0,
        scrapCnt = 0,
        like = false,
        scrap = false
    )

    private fun fallbackCommentInfo(commentId: Long) = CommentFavoriteInfo(
        postCommentId = commentId,
        count = 0,
        like = false
    )
}
