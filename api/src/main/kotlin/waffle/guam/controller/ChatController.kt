package waffle.guam.controller

import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import waffle.guam.common.UserContext
import waffle.guam.controller.request.ContentInput
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
        @PageableDefault(size = 10, page = 0, sort = ["id"], direction = Sort.Direction.ASC) pageable: Pageable
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
        @RequestParam("images") imageFiles: List<MultipartFile>?,
        @RequestBody contentInput: ContentInput?,
        userContext: UserContext
    ): SuccessResponse<Boolean> =
        SuccessResponse(
            chatService.createThread(
                command = CreateThread(
                    projectId = projectId,
                    userId = userContext.id,
                    content = contentInput?.content,
                    imageFiles = imageFiles
                )
            )
        )

    @PutMapping("/thread/{threadId}/content")
    fun editThreadContent(
        @PathVariable threadId: Long,
        @RequestBody contentInput: ContentInput,
        userContext: UserContext
    ): SuccessResponse<Boolean> =
        SuccessResponse(
            chatService.editThreadContent(
                command = EditThreadContent(threadId = threadId, userId = userContext.id, content = contentInput.content)
            )
        )

    @DeleteMapping("/thread/{threadId}/image/{imageId}")
    fun deleteThreadImage(
        @PathVariable threadId: Long,
        @PathVariable imageId: Long,
        userContext: UserContext
    ): SuccessResponse<Boolean> =
        SuccessResponse(
            chatService.deleteThreadImage(
                command = DeleteThreadImage(imageId = imageId, threadId = threadId, userId = userContext.id)
            )
        )

    @DeleteMapping("/thread/{threadId}")
    fun deleteThread(
        @PathVariable threadId: Long,
        userContext: UserContext
    ): SuccessResponse<Boolean> =
        SuccessResponse(
            chatService.deleteThread(
                command = DeleteThread(threadId = threadId, userId = userContext.id)
            )
        )

    @PostMapping("/comment/create/{threadId}")
    fun createComment(
        @PathVariable threadId: Long,
        @RequestParam("images") imageFiles: List<MultipartFile>?,
        @RequestBody contentInput: ContentInput?,
        userContext: UserContext
    ): SuccessResponse<Boolean> =
        SuccessResponse(
            chatService.createComment(
                command = CreateComment(
                    threadId = threadId,
                    userId = userContext.id,
                    content = contentInput?.content,
                    imageFiles = imageFiles
                )
            )
        )

    @PutMapping("/comment/{commentId}/content")
    fun editCommentContent(
        @PathVariable commentId: Long,
        @RequestBody contentInput: ContentInput,
        userContext: UserContext
    ): SuccessResponse<Boolean> =
        SuccessResponse(
            chatService.editCommentContent(
                command = EditCommentContent(commentId = commentId, userId = userContext.id, content = contentInput.content)
            )
        )

    @DeleteMapping("/comment/{commentId}/image/{imageId}")
    fun deleteCommentImage(
        @PathVariable commentId: Long,
        @PathVariable imageId: Long,
        userContext: UserContext
    ): SuccessResponse<Boolean> =
        SuccessResponse(
            chatService.deleteCommentImage(
                command = DeleteCommentImage(imageId = imageId, commentId = commentId, userId = userContext.id)
            )
        )

    @DeleteMapping("/comment/{commentId}")
    fun deleteComment(
        @PathVariable commentId: Long,
        userContext: UserContext
    ): SuccessResponse<Boolean> =
        SuccessResponse(
            chatService.deleteComment(
                command = DeleteComment(commentId = commentId, userId = userContext.id)
            )
        )
}
