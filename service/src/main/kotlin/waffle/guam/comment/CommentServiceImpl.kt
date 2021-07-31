package waffle.guam.comment

import java.time.LocalDateTime
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import waffle.guam.db.entity.ImageType
import waffle.guam.db.repository.CommentRepository
import waffle.guam.db.repository.ImageRepository
import waffle.guam.comment.command.CreateComment
import waffle.guam.comment.command.DeleteComment
import waffle.guam.comment.command.DeleteCommentImage
import waffle.guam.comment.command.EditCommentContent
import waffle.guam.comment.event.CommentCreated
import waffle.guam.comment.event.CommentDeleted

@Service
class CommentServiceImpl(
    private val commentRepository: CommentRepository,
    private val imageRepository: ImageRepository,
) : CommentService {

    @Transactional
    override fun createComment(command: CreateComment): CommentCreated {
        val commentId = if (command.content.isNullOrBlank()) {
            commentRepository.save(command.copy(content = null).toEntity()).id
        } else {
            commentRepository.save(command.copy(content = command.content.trim()).toEntity()).id
        }
        return CommentCreated(commentId)
        // TODO(event handler)
        //        if (!command.imageFiles.isNullOrEmpty())
        //            for (imageFile in command.imageFiles)
        //                imageService.upload(imageFile, ImageInfo(commentId, ImageType.COMMENT))
    }

    @Transactional
    override fun editCommentContent(command: EditCommentContent): Boolean {
            if (command.content.isBlank()) {
                if (imageRepository.findByParentIdAndType(command.commentId, ImageType.COMMENT).isEmpty()) {
                    this.deleteComment(
                        DeleteComment(commentId = command.commentId, userId = command.userId, targetComment = command.currentComment)
                    )
                } else {
                    commentRepository.save(command.currentComment!!.copy(content = null, modifiedAt = LocalDateTime.now()))
                }
            } else {
                commentRepository.save(command.currentComment!!.copy(content = command.content.trim(), modifiedAt = LocalDateTime.now()))
            }
        return true
    }

    @Transactional
    override fun deleteCommentImage(command: DeleteCommentImage): Boolean {
        if (command.parentComment!!.content.isNullOrBlank()) {
            if (imageRepository.findByParentIdAndType(command.parentComment.id, ImageType.COMMENT).size < 2)
                commentRepository.delete(command.parentComment)
        }
        command.targetImage?.let { imageRepository.delete(it) }
        return true
    }

    @Transactional
    override fun deleteComment(command: DeleteComment): CommentDeleted {
        command.targetComment?.let { commentRepository.delete(it) }
            return CommentDeleted(commentId = command.commentId, targetComment = command.targetComment)
            //    TODO(event handler)
            //    imageRepository.deleteByParentIdAndType(command.commentId, ImageType.COMMENT)
            //    if (commentRepository.findByThreadId(it.threadId).isEmpty())
            //        if (threadRepository.findById(it.threadId).get().content.isNullOrBlank())
            //            if (imageRepository.findByParentIdAndType(it.threadId, ImageType.THREAD).isEmpty())
            //                threadRepository.deleteById(it.threadId
    }
}