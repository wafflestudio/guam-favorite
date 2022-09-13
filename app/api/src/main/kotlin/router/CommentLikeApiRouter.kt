package waffle.guam.favorite.api.router

import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import waffle.guam.favorite.api.model.SuccessResponse
import waffle.guam.favorite.service.command.CommentLikeCreateHandler
import waffle.guam.favorite.service.command.CommentLikeDeleteHandler
import waffle.guam.favorite.service.model.CommentLike

@Service
class CommentLikeApiRouter(
    private val commentLikeCreateHandler: CommentLikeCreateHandler,
    private val commentLikeDeleteHandler: CommentLikeDeleteHandler,
) {

    suspend fun create(request: ServerRequest): ServerResponse {
        val userId = request.getHeader("X-GATEWAY-USER-ID").toLong()
        val postCommentId = request.pathVariable("postCommentId").toLong()

        commentLikeCreateHandler.handle(
            CommentLike(
                postCommentId = postCommentId,
                userId = userId
            )
        )

        return ServerResponse.ok().bodyValueAndAwait(SuccessResponse(Unit))
    }

    suspend fun delete(request: ServerRequest): ServerResponse {
        val userId = request.getHeader("X-GATEWAY-USER-ID").toLong()
        val postCommentId = request.pathVariable("postCommentId").toLong()

        commentLikeDeleteHandler.handle(
            CommentLike(
                postCommentId = postCommentId,
                userId = userId
            )
        )

        return ServerResponse.ok().bodyValueAndAwait(SuccessResponse(Unit))
    }
}
