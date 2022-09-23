package waffle.guam.favorite.api.router

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.queryParamOrNull
import waffle.guam.favorite.api.model.CommentFavoriteInfo
import waffle.guam.favorite.api.model.PostFavoriteInfo
import waffle.guam.favorite.api.model.SuccessResponse
import waffle.guam.favorite.data.redis.repository.CommentLikeCountRepository
import waffle.guam.favorite.data.redis.repository.PostLikeCountRepository
import waffle.guam.favorite.data.redis.repository.PostScrapCountRepository
import waffle.guam.favorite.service.query.CommentLikeUserStore
import waffle.guam.favorite.service.query.LikeUserStore
import waffle.guam.favorite.service.query.ScrapUserStore

@RequestMapping("/api/v1/views")
@RestController
class ViewApiRouter(
    private val likeCountRepository: PostLikeCountRepository,
    private val likeUserStore: LikeUserStore,
    private val scrapCountRepository: PostScrapCountRepository,
    private val scrapUserStore: ScrapUserStore,
    private val commentLikeCountRepository: CommentLikeCountRepository,
    private val commentLikeUserStore: CommentLikeUserStore,
) {

    suspend fun getPostInfos(request: ServerRequest): ServerResponse = coroutineScope {
        val postIds = request.getParam("postIds")
            .takeIf { it.isNotBlank() }
            ?.split(",")
            ?.map { it.toLong() }
            ?: emptyList()
        val userId = request.getParam("userId").toLong()

        val likeCntMap = async { likeCountRepository.gets(postIds) }
        val likeMap = async { likeUserStore.getLiked(postIds, userId) }
        val scrapCntMap = async { scrapCountRepository.gets(postIds) }
        val scrapMap = async { scrapUserStore.getScraped(postIds, userId) }

        val response = postIds.map {
            PostFavoriteInfo(
                postId = it,
                likeCnt = likeCntMap.await()[it]!!,
                scrapCnt = scrapCntMap.await()[it]!!,
                like = likeMap.await()[it]!!,
                scrap = scrapMap.await()[it]!!,
            )
        }

        ServerResponse.ok().bodyValueAndAwait(SuccessResponse(response))
    }

    suspend fun getCommentInfos(request: ServerRequest): ServerResponse {
        val postCommentIds = request.getParam("postCommentIds")
            .takeIf { it.isNotBlank() }
            ?.split(",")
            ?.map { it.toLong() }
            ?: emptyList()
        val userId = request.getParam("userId").toLong()

        val response = coroutineScope {
            val countMap = async { commentLikeCountRepository.gets(postCommentIds) }
            val likedMap = async { commentLikeUserStore.getLiked(postCommentIds, userId) }

            postCommentIds.map {
                CommentFavoriteInfo(
                    postCommentId = it,
                    count = countMap.await()[it]!!,
                    like = likedMap.await()[it]!!
                )
            }
        }

        return ServerResponse.ok().bodyValueAndAwait(SuccessResponse(response))
    }

    suspend fun getPostRank(request: ServerRequest): ServerResponse {
        val boardId = request.queryParamOrNull("boardId")?.toLong()
        val from = request.getParam("from").toLong()
        val to = request.getParam("to").toLong()

        val rank = likeCountRepository.getRank(boardId = boardId, from = from, to = to)

        return ServerResponse.ok().bodyValueAndAwait(SuccessResponse(rank))
    }

    suspend fun getScrappedPosts(request: ServerRequest): ServerResponse {
        val userId = request.getParam("userId").toLong()
        val page = request.getParam("page").toInt()

        val response = scrapUserStore.getScrappedPostIds(userId, page)

        return ServerResponse.ok().bodyValueAndAwait(SuccessResponse(response))
    }
}
