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
        return CommentCreated(commentId, command.imageFiles)
        //   TODO(event handler : 생성한 댓글 ID와 업로드하려는 imageFiles 정보 필요)
        //    if (!command.imageFiles.isNullOrEmpty())
        //        for (imageFile in command.imageFiles)
        //            imageService.upload(imageFile, ImageInfo(commentId, ImageType.COMMENT))
    }

    @Transactional
    override fun editCommentContent(command: EditCommentContent): Boolean {
        if (command.content.isNotBlank()) {
            commentRepository.updateContent(id = command.commentId, content = command.content.trim(), modifiedAt = LocalDateTime.now())
            return true
        }
        if (imageRepository.findByParentIdAndType(command.commentId, ImageType.COMMENT).isNotEmpty()) {
            commentRepository.updateContent(id = command.commentId, content = null, modifiedAt = LocalDateTime.now())
            return true
        }
        this.deleteComment(DeleteComment(commentId = command.commentId, userId = command.userId))
        return true
    }

    @Transactional
    override fun deleteCommentImage(command: DeleteCommentImage): Boolean {
        if (command.commentContent.isNullOrBlank()) {
            if (imageRepository.findByParentIdAndType(command.commentId, ImageType.COMMENT).size < 2)
                commentRepository.deleteById(command.commentId)
        }
        imageRepository.deleteById(command.imageId)
        return true
    }

    @Transactional
    override fun deleteComment(command: DeleteComment): CommentDeleted {
        commentRepository.deleteById(command.commentId)
        return CommentDeleted(command.commentId, command.threadId)
        //   TODO(event handler : 삭제한 댓글의 id & threadId 정보 필요)
        //    imageRepository.deleteByParentIdAndType(command.commentId, ImageType.COMMENT)
        //    if (commentRepository.findByThreadId(it.threadId).isEmpty())
        //        if (threadRepository.findById(it.threadId).get().content.isNullOrBlank())
        //            if (imageRepository.findByParentIdAndType(it.threadId, ImageType.THREAD).isEmpty())
        //                threadRepository.deleteById(it.threadId)
    }
}