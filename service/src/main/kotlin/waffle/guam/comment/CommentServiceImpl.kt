package waffle.guam.comment

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import waffle.guam.comment.command.CreateComment
import waffle.guam.comment.command.DeleteComment
import waffle.guam.comment.command.DeleteCommentImage
import waffle.guam.comment.command.EditCommentContent
import waffle.guam.comment.event.CommentContentEdited
import waffle.guam.comment.event.CommentCreated
import waffle.guam.comment.event.CommentDeleted
import waffle.guam.comment.event.CommentImageDeleted
import waffle.guam.db.entity.ImageType
import waffle.guam.db.repository.CommentRepository
import waffle.guam.db.repository.ImageRepository
import waffle.guam.exception.InvalidRequestException
import java.time.LocalDateTime

@Service
class CommentServiceImpl(
    private val commentRepository: CommentRepository,
    private val imageRepository: ImageRepository,
) : CommentService {

    @Transactional
    override fun createComment(command: CreateComment): CommentCreated {
        val createdComment = if (command.content.isNullOrBlank()) {
            commentRepository.save(command.copy(content = null).toEntity())
        } else {
            commentRepository.save(command.copy(content = command.content.trim()).toEntity())
        }
        return CommentCreated(createdComment.id, command.imageFiles)
    } /*TODO(event listener : 생성한 댓글 ID와 업로드하려는 imageFiles 정보 필요)
             if (!command.imageFiles.isNullOrEmpty())
                 for (imageFile in command.imageFiles)
                     imageService.upload(imageFile, ImageInfo(commentId, ImageType.COMMENT))*/

    @Transactional
    override fun editCommentContent(command: EditCommentContent): CommentContentEdited {
        val currentComment = commentRepository.findById(command.commentId).get()
        if (command.content.isNotBlank()) {
            commentRepository.save(currentComment.copy(content = command.content.trim(), modifiedAt = LocalDateTime.now()))
            return CommentContentEdited(currentComment.id)
        }
        if (imageRepository.findByParentIdAndType(command.commentId, ImageType.COMMENT).isNotEmpty()) {
            commentRepository.save(currentComment.copy(content = null, modifiedAt = LocalDateTime.now()))
            return CommentContentEdited(currentComment.id)
        }
        throw InvalidRequestException("해당 댓글이 삭제됩니다")
    } /* TODO(클라이언트 추가 작업: 내용이 비게 되는 경우 editCommentContent가 아닌 deleteComment가 호출되도록 조건문 추가)
          this.deleteComment(DeleteComment(commentId = command.commentId, userId = command.userId))
          return CommentContentEdited(currentComment.id)*/

    @Transactional
    override fun deleteCommentImage(command: DeleteCommentImage): CommentImageDeleted {
        imageRepository.deleteById(command.imageId)
        return CommentImageDeleted(command.commentId, command.imageId)
    } /* TODO(클라이언트 추가 작업: 마지막 이미지를 삭제하려는 경우 deleteCommentImage가 아닌 deleteComment가 호출되도록 조건문 추가)
            val parentComment = commentRepository.findById(command.commentId).get()
             if (parentComment.content.isNullOrBlank()) {
                 if (imageRepository.findByParentIdAndType(parentComment.id, ImageType.COMMENT).size < 2)
                     commentRepository.deleteById(command.commentId)
             }*/

    @Transactional
    override fun deleteComment(command: DeleteComment): CommentDeleted {
        val targetComment = commentRepository.findById(command.commentId).get()
        commentRepository.deleteById(command.commentId)
        return CommentDeleted(command.commentId, targetComment.threadId)
    } /*TODO(event listener : 삭제한 댓글의 id & threadId 정보 필요)
         imageRepository.deleteByParentIdAndType(command.commentId, ImageType.COMMENT)
         if (commentRepository.findByThreadId(it.threadId).isEmpty())
             if (threadRepository.findById(it.threadId).get().content.isNullOrBlank())
                 if (imageRepository.findByParentIdAndType(it.threadId, ImageType.THREAD).isEmpty())
                     threadRepository.deleteById(it.threadId)*/
}
