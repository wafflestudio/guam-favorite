package waffle.guam.controller

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import waffle.guam.NotAllowedException
import waffle.guam.comment.CommentService
import waffle.guam.comment.command.CreateComment
import waffle.guam.comment.command.DeleteComment
import waffle.guam.comment.command.EditCommentContent
import waffle.guam.comment.event.CommentContentEdited
import waffle.guam.comment.event.CommentCreated
import waffle.guam.comment.event.CommentDeleted
import waffle.guam.common.UserContext
import waffle.guam.controller.request.ContentInput
import waffle.guam.controller.request.CreateFullInfoInput
import waffle.guam.controller.response.SuccessResponse
import waffle.guam.image.ImageService
import waffle.guam.image.command.DeleteImages
import waffle.guam.image.event.ImagesDeleted

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
    ): SuccessResponse<CommentCreated> =
        SuccessResponse(
            commentService.createComment(
                command = CreateComment(
                    threadId = threadId,
                    userId = userContext.id,
                    content = createFullInfoInput.content,
                    imageFiles = createFullInfoInput.imageFiles
                )
            )
        )

    @PutMapping("/{commentId}/content")
    fun editCommentContent(
        @PathVariable commentId: Long,
        @RequestBody contentInput: ContentInput,
        userContext: UserContext
    ): SuccessResponse<CommentContentEdited> =
        SuccessResponse(
            commentService.editCommentContent(
                command = EditCommentContent(
                    commentId = commentId,
                    userId = userContext.id,
                    content = contentInput.content
                )
            )
        )

    @DeleteMapping("/{commentId}/image/{imageId}")
    fun deleteCommentImage(
        @PathVariable commentId: Long,
        @PathVariable imageId: Long,
        userContext: UserContext
    ): SuccessResponse<ImagesDeleted> =
        commentService.getComment(commentId).let {
            if (it.creatorId != userContext.id) {
                throw NotAllowedException("타인이 업로드한 이미지를 삭제할 수는 없습니다.")
            }
            return SuccessResponse(
                imageService.deleteImages(DeleteImages.ById(listOf(imageId)))
            )
        }

    @DeleteMapping("/{commentId}")
    fun deleteComment(
        @PathVariable commentId: Long,
        userContext: UserContext
    ): SuccessResponse<CommentDeleted> =
        SuccessResponse(
            commentService.deleteComment(
                command = DeleteComment(commentId = commentId, userId = userContext.id)
            )
        )
}