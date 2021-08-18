package waffle.guam.api

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import waffle.guam.NotAllowedException
import waffle.guam.api.request.ContentInput
import waffle.guam.api.request.CreateFullInfoInput
import waffle.guam.api.response.SuccessResponse
import waffle.guam.comment.CommentService
import waffle.guam.comment.command.CreateComment
import waffle.guam.comment.command.DeleteComment
import waffle.guam.comment.command.EditCommentContent
import waffle.guam.common.UserContext
import waffle.guam.image.ImageService
import waffle.guam.image.command.DeleteImages

@RestController
@RequestMapping("comment")
class CommentController(
    private val commentService: CommentService,
    private val imageService: ImageService
) {

    @PostMapping("/create/{threadId}")
    fun createComment(
        @PathVariable threadId: Long,
        @ModelAttribute("contentAndOrImages") createFullInfoInput: CreateFullInfoInput,
        userContext: UserContext
    ): SuccessResponse<Unit> =
        commentService.createComment(
            command = CreateComment(
                threadId = threadId,
                userId = userContext.id,
                content = createFullInfoInput.content,
                imageFiles = createFullInfoInput.imageFiles
            )
        ).let { SuccessResponse(Unit) }

    @PutMapping("/{commentId}/content")
    fun editCommentContent(
        @PathVariable commentId: Long,
        @RequestBody contentInput: ContentInput,
        userContext: UserContext
    ): SuccessResponse<Unit> =
        commentService.editCommentContent(
            command = EditCommentContent(
                commentId = commentId,
                userId = userContext.id,
                content = contentInput.content
            )
        ).let { SuccessResponse(Unit) }

    @DeleteMapping("/{commentId}/image/{imageId}")
    fun deleteCommentImage(
        @PathVariable commentId: Long,
        @PathVariable imageId: Long,
        userContext: UserContext
    ): SuccessResponse<Unit> =
        commentService.getComment(commentId).let {
            if (it.creatorId != userContext.id) {
                throw NotAllowedException("타인이 업로드한 이미지를 삭제할 수는 없습니다.")
            }
            imageService.deleteImages(DeleteImages.ById(listOf(imageId)))
            return SuccessResponse(Unit)
        }

    @DeleteMapping("/{commentId}")
    fun deleteComment(
        @PathVariable commentId: Long,
        userContext: UserContext
    ): SuccessResponse<Unit> =
        commentService.deleteComment(
            command = DeleteComment(commentId = commentId, userId = userContext.id)
        ).let { SuccessResponse(Unit) }
}
