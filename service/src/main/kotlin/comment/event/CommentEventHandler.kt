package waffle.guam.comment.event

import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import waffle.guam.image.ImageService
import waffle.guam.image.command.CreateImages
import waffle.guam.image.command.DeleteImages
import waffle.guam.image.model.ImageType
import waffle.guam.thread.ThreadRepository
import waffle.guam.thread.ThreadService

@Component
class CommentEventHandler(
    private val imageService: ImageService,
    private val threadService: ThreadService,
    private val threadRepository: ThreadRepository, // FIXME: ThreadService 수정하면서 대체
) {

    @EventListener
    fun handleCommentCreated(event: CommentCreated) {
        if (!event.imageFiles.isNullOrEmpty())
            imageService.createImages(
                CreateImages(
                    files = event.imageFiles,
                    type = ImageType.COMMENT,
                    parentId = event.commentId
                )
            )
    }

    @EventListener
    fun handleCommentContentEdited(event: CommentContentEdited) {
        /* TODO(클라이언트 추가 작업?: 내용이 비게 되는 경우 editCommentContent가 아닌 deleteComment가 호출되도록 조건문 추가)
            this.deleteComment(DeleteComment(commentId = command.commentId, userId = command.userId)) */
    }

//    @EventListener
//    fun handleCommentImageDeleted(event: CommentImageDeleted) {
        /* TODO(클라이언트 추가 작업?: 마지막 이미지를 삭제하려는 경우 deleteCommentImage가 아닌 deleteComment가 호출되도록 조건문 추가)
               val parentComment = commentRepository.findById(command.commentId).get()
                if (parentComment.content.isNullOrBlank()) {
                    if (imageRepository.findByParentIdAndType(parentComment.id, ImageType.COMMENT).size < 2)
                        commentRepository.deleteById(command.commentId)
                }*/
//    }

    @EventListener
    fun handleCommentDeleted(event: CommentDeleted) {
        imageService.deleteImages(DeleteImages.ByParentId(event.commentId, ImageType.COMMENT))
        threadService.getFullThread(event.threadId).let {
            if (it.comments.isEmpty() && it.content.isNullOrBlank() && it.threadImages.isEmpty())
                threadRepository.deleteById(event.threadId) // FIXME : 쓰레드 생성자가 아닌 경우 쓰레드 제거하는 서비스 추가하여 대체
        }
    }
}
