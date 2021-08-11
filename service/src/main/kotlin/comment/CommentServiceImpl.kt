
package waffle.guam.comment

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import waffle.guam.InvalidRequestException
import waffle.guam.comment.command.CreateComment
import waffle.guam.comment.command.DeleteComment
import waffle.guam.comment.command.DeleteCommentImage
import waffle.guam.comment.command.EditCommentContent
import waffle.guam.comment.event.CommentContentEdited
import waffle.guam.comment.event.CommentCreated
import waffle.guam.comment.event.CommentDeleted
import waffle.guam.comment.event.CommentImageDeleted
import waffle.guam.image.ImageService
import waffle.guam.image.command.DeleteImages
import java.time.Instant

@Service
class CommentServiceImpl(
    private val commentRepository: CommentRepository,
    private val imageService: ImageService,
) : CommentService {

    @Transactional
    override fun createComment(command: CreateComment): CommentCreated {
        val createdComment = if (command.content.isNullOrBlank()) {
            commentRepository.save(command.copy(content = null).toEntity())
        } else {
            commentRepository.save(command.copy(content = command.content.trim()).toEntity())
        }
        return CommentCreated(createdComment.id, command.imageFiles)
    }

    @Transactional
    override fun editCommentContent(command: EditCommentContent): CommentContentEdited {
        val currentComment = commentRepository.findById(command.commentId).get()
        if (command.content.isNotBlank()) {
            commentRepository.save(currentComment.copy(content = command.content.trim(), modifiedAt = Instant.now()))
            return CommentContentEdited(currentComment.id)
        }
        // TODO(부모에 달린 이미지들 조회하는 ImageService 추가? VS content=null은 빈문자열로 전부 대체)
        //    if (imageRepository.findByParentIdAndType(command.commentId, ImageType.COMMENT).isNotEmpty()) {
        //        commentRepository.save(currentComment.copy(content = null, modifiedAt = Instant.now()))
        //        return CommentContentEdited(currentComment.id)
        //    }
        throw InvalidRequestException("해당 댓글이 삭제됩니다")
    }

    @Transactional
    override fun deleteCommentImage(command: DeleteCommentImage): CommentImageDeleted {
        imageService.deleteImages(DeleteImages.ById(listOf(command.imageId)))
        return CommentImageDeleted(command.commentId, command.imageId)
    }

    @Transactional
    override fun deleteComment(command: DeleteComment): CommentDeleted {
        val targetComment = commentRepository.findById(command.commentId).get()
        commentRepository.deleteById(command.commentId)
        return CommentDeleted(command.commentId, targetComment.threadId)
    }
}
