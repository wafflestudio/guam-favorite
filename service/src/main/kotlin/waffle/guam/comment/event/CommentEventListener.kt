package waffle.guam.comment.event

import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import waffle.guam.db.entity.ImageType
import waffle.guam.db.repository.CommentRepository
import waffle.guam.db.repository.ImageRepository
import waffle.guam.db.repository.ThreadRepository
import waffle.guam.image.ImageService
import waffle.guam.image.command.CreateImages

@Component
class CommentEventListener(
    private val imageService: ImageService,
    private val imageRepository: ImageRepository,
    private val commentRepository: CommentRepository,
    private val threadRepository: ThreadRepository
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
        /* TODO(클라이언트 추가 작업: 내용이 비게 되는 경우 editCommentContent가 아닌 deleteComment가 호출되도록 조건문 추가)
            this.deleteComment(DeleteComment(commentId = command.commentId, userId = command.userId))
              return CommentContentEdited(currentComment.id)*/
    }

    @EventListener
    fun handleCommentImageDeleted(event: CommentImageDeleted) {
        /* TODO(클라이언트 추가 작업: 마지막 이미지를 삭제하려는 경우 deleteCommentImage가 아닌 deleteComment가 호출되도록 조건문 추가)
               val parentComment = commentRepository.findById(command.commentId).get()
                if (parentComment.content.isNullOrBlank()) {
                    if (imageRepository.findByParentIdAndType(parentComment.id, ImageType.COMMENT).size < 2)
                        commentRepository.deleteById(command.commentId)
                }*/
    }

    // FIXME: Thread에서 별도의 서비스 생성하여 사용? 클라와 논의? 기획 수정?
    @EventListener
    fun handleCommentDeleted(event: CommentDeleted) {
        imageRepository.deleteByParentIdAndType(event.commentId, ImageType.COMMENT)
        if (commentRepository.findByThreadId(event.threadId).isEmpty())
            if (threadRepository.findById(event.threadId).get().content.isNullOrBlank())
                if (imageRepository.findByParentIdAndType(event.threadId, ImageType.THREAD).isEmpty())
                    threadRepository.deleteById(event.threadId)
    }
}
