package waffle.guam.comment.event

import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import waffle.guam.comment.CommentRepository
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
    private val threadRepository: ThreadRepository,
    private val commentRepository: CommentRepository,
    // private val messageService: MessageService,
) {
    private val logger = LoggerFactory.getLogger(this::javaClass.name)

    @EventListener
    fun handle(event: CommentCreated) {
        if (!event.imageFiles.isNullOrEmpty()) {
            imageService.createImages(
                CreateImages(
                    files = event.imageFiles,
                    type = ImageType.COMMENT,
                    parentId = event.commentId
                )
            )
        }

        logger.info("$event")

        // if (event.threadCreatorId == event.commentCreatorId) return

        // messageService.sendMessage(
        //     ids = listOf(event.threadCreatorId),
        //     title = "새로운 댓글이 달렸습니다.",
        //     body = "${event.commentCreatorName}: ${event.content}"
        // )
    }

    @EventListener
    fun handle(event: CommentContentEdited) {
        if (event.commentContent.isBlank() && event.commentImages.isNullOrEmpty()) {
            commentRepository.deleteById(event.commentId)
            this.deleteThreadIfEmpty(event.parentThreadId)
        }
        logger.info("$event")
    }

    // CommentImageDeleted
    /* TODO(클라이언트 추가 작업?: 마지막 이미지를 삭제하려는 경우 deleteCommentImage가 아닌 deleteComment가 호출되도록 조건문 추가)
           val parentComment = commentRepository.findById(command.commentId).get()
            if (parentComment.content.isNullOrBlank()) {
                if (imageRepository.findByParentIdAndType(parentComment.id, ImageType.COMMENT).size < 2)
                    commentRepository.deleteById(command.commentId)
            }*/

    @EventListener
    fun handle(event: CommentDeleted) {
        imageService.deleteImages(DeleteImages.ByParentId(event.commentId, ImageType.COMMENT))
        this.deleteThreadIfEmpty(event.threadId)
        logger.info("$event")
    }

    private fun deleteThreadIfEmpty(threadId: Long) {
        threadService.getFullThread(threadId).let {
            if (it.comments.isEmpty() && it.content.isNullOrBlank() && it.threadImages.isEmpty()) {
                threadRepository.deleteById(threadId)
            }
        }
    }
}
