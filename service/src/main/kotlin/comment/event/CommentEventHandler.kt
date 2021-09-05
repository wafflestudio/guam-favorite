package waffle.guam.comment.event

import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import waffle.guam.DataNotFoundException
import waffle.guam.comment.CommentRepository
import waffle.guam.image.ImageRepository
import waffle.guam.image.ImageService
import waffle.guam.image.command.CreateImages
import waffle.guam.image.command.DeleteImages
import waffle.guam.image.model.ImageType
import waffle.guam.thread.ThreadRepository
import waffle.guam.thread.ThreadService
import waffle.guam.thread.model.ThreadType

@Component
class CommentEventHandler(
    private val imageService: ImageService,
    private val threadService: ThreadService,
    private val threadRepository: ThreadRepository,
    private val commentRepository: CommentRepository,
    private val imageRepository: ImageRepository,
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

    @EventListener
    fun handle(event: CommentImageDeleted) {
        logger.info("$event")

        commentRepository.findById(event.commentId).orElseThrow(::DataNotFoundException).let {

            if (imageRepository.findByParentIdAndType(it.id, ImageType.COMMENT.name).isNotEmpty()) return

            commentRepository.delete(it)

            this.deleteThreadIfEmpty(event.parentThreadId)
        }
    }

    @EventListener
    fun handle(event: CommentDeleted) {
        imageService.deleteImages(DeleteImages.ByParentId(event.commentId, ImageType.COMMENT))
        this.deleteThreadIfEmpty(event.parentThreadId)

        logger.info("$event")
    }

    private fun deleteThreadIfEmpty(threadId: Long) {
        threadService.getFullThread(threadId).let {

            if (it.type != ThreadType.NORMAL) return

            if (it.comments.isEmpty() && it.content.isBlank() && it.threadImages.isEmpty()) {
                threadRepository.deleteById(threadId)
            }
        }
    }
}
