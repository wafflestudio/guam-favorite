package waffle.guam.controller

import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import waffle.guam.controller.request.EditChatInput
import waffle.guam.controller.request.CreateChatInput
import waffle.guam.controller.response.PageableResponse
import waffle.guam.controller.response.SuccessResponse
import waffle.guam.model.ThreadDetail
import waffle.guam.model.ThreadOverView
import waffle.guam.service.ChatService
import waffle.guam.service.command.*

@RestController
@RequestMapping
class ChatController(
    private val chatService: ChatService
) {
    @GetMapping("/project/{projectId}/threads")
    fun getThreads(
        @PathVariable projectId: Long,
        @PageableDefault(size = 10, page = 0, sort = ["id"], direction = Sort.Direction.DESC) pageable: Pageable
    ): PageableResponse<ThreadOverView> =
        chatService.getThreads(projectId, pageable).let {
            PageableResponse(
                data = it.content,
                size = it.content.size,
                offset = it.pageable.offset.toInt(),
                totalCount = it.totalElements.toInt(),
                hasNext = it.pageable.offset + it.size < it.totalElements
            )
        }

    @GetMapping("/thread/{threadId}")
    fun getFullThread(
        @PathVariable threadId: Long
    ): SuccessResponse<ThreadDetail> =
        SuccessResponse(
            chatService.getFullThread(threadId)
        )

    @PostMapping("/thread/create/{projectId}")
    fun createThread(
        @PathVariable projectId: Long,
        @RequestBody createChatInput: CreateChatInput,
        @RequestHeader("USER-ID") userId: Long

    ): SuccessResponse<Boolean> =
        SuccessResponse(
            chatService.createThread(
                command = CreateThread(
                    projectId = projectId,
                    userId = userId,
                    content = createChatInput.content,
                    imageUrls = createChatInput.imageUrls
                )
            )
        )

    @PutMapping("/thread/{threadId}/content")
    fun editThreadContent(
        @PathVariable threadId: Long,
        @RequestBody editChatInput: EditChatInput,
        @RequestHeader("USER-ID") userId: Long
    ): SuccessResponse<Boolean> =
        SuccessResponse(
            chatService.editThreadContent(
                command = EditThreadContent(threadId = threadId, userId = userId, content = editChatInput.content)
            )
        )

    @DeleteMapping("/thread/{threadId}/image/{imageId}")
    fun deleteThreadImage(
        @PathVariable threadId: Long,
        @PathVariable imageId: Long,
        @RequestHeader("USER-ID") userId: Long
    ): SuccessResponse<Boolean> =
        SuccessResponse(
            chatService.deleteThreadImage(
                command = DeleteThreadImage(imageId = imageId, threadId = threadId, userId = userId)
            )
        )

    @DeleteMapping("/thread/{threadId}")
    fun deleteThread(
        @PathVariable threadId: Long,
        @RequestHeader("USER-ID") userId: Long
    ): SuccessResponse<Boolean> =
        SuccessResponse(
            chatService.deleteThread(
                command = DeleteThread(threadId = threadId, userId = userId)
            )
        )

    @PostMapping("/comment/create/{threadId}")
    fun createComment(
        @PathVariable threadId: Long,
        @RequestBody createChatInput: CreateChatInput,
        @RequestHeader("USER-ID") userId: Long
    ): SuccessResponse<Boolean> =
        SuccessResponse(
            chatService.createComment(
                command = CreateComment(
                    threadId = threadId,
                    userId = userId,
                    content = createChatInput.content,
                    imageUrls = createChatInput.imageUrls
                )
            )
        )

    @PutMapping("/comment/{commentId}/content")
    fun editCommentContent(
        @PathVariable commentId: Long,
        @RequestBody editChatInput: EditChatInput,
        @RequestHeader("USER-ID") userId: Long
    ): SuccessResponse<Boolean> =
        SuccessResponse(
            chatService.editCommentContent(
                command = EditCommentContent(commentId = commentId, userId = userId, content = editChatInput.content)
            )
        )

    @DeleteMapping("/comment/{commentId}/image/{imageId}")
    fun deleteCommentImage(
        @PathVariable commentId: Long,
        @PathVariable imageId: Long,
        @RequestHeader("USER-ID") userId: Long
    ): SuccessResponse<Boolean> =
        SuccessResponse(
            chatService.deleteCommentImage(
                command = DeleteCommentImage(imageId = imageId, commentId = commentId, userId = userId)
            )
        )


    @DeleteMapping("/comment/{commentId}")
    fun deleteComment(
        @PathVariable commentId: Long,
        @RequestHeader("USER-ID") userId: Long
    ): SuccessResponse<Boolean> =
        SuccessResponse(
            chatService.deleteComment(
                command = DeleteComment(commentId = commentId, userId = userId)
            )
        )
}
