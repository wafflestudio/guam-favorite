package waffle.guam.comment

import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import waffle.guam.db.entity.ImageType
import waffle.guam.db.repository.CommentRepository
import waffle.guam.db.repository.ImageRepository
import waffle.guam.db.repository.ThreadRepository
import waffle.guam.exception.InvalidRequestException
import waffle.guam.exception.NotAllowedException
import waffle.guam.comment.command.CreateComment
import waffle.guam.comment.command.DeleteComment
import waffle.guam.comment.command.DeleteCommentImage
import waffle.guam.comment.command.EditCommentContent
import waffle.guam.comment.event.CommentCreated
import waffle.guam.comment.event.CommentDeleted
import waffle.guam.exception.DataNotFoundException

@Primary
@Service
class CommentServiceImplPrimary(
    private val threadRepository: ThreadRepository,
    private val commentRepository: CommentRepository,
    private val imageRepository: ImageRepository,
    private val impl: CommentServiceImpl
) : CommentService {

    @Transactional
    override fun createComment(command: CreateComment): CommentCreated {
        threadRepository.findById(command.threadId).orElseThrow(::DataNotFoundException)
        return impl.createComment(command)
    }

    @Transactional
    override fun editCommentContent(command: EditCommentContent): Boolean {
        commentRepository.findById(command.commentId).orElseThrow(::DataNotFoundException).let {
            if (it.userId != command.userId) throw NotAllowedException()
            if (it.content == command.content) throw InvalidRequestException("수정 전과 동일한 내용입니다.")
            return impl.editCommentContent(command.copy(currentComment = it))
        }
    }

    @Transactional
    override fun deleteCommentImage(command: DeleteCommentImage): Boolean {
        imageRepository.findById(command.imageId).orElseThrow(::DataNotFoundException).let { image ->
            commentRepository.findById(command.commentId).orElseThrow(::DataNotFoundException).also {
                if (it.userId != command.userId) throw NotAllowedException()
                if (it.id != image.parentId) throw InvalidRequestException()
                return impl.deleteCommentImage(command.copy(parentComment = it, targetImage = image))
            }
        }
    }

    @Transactional
    override fun deleteComment(command: DeleteComment): CommentDeleted {
        commentRepository.findById(command.commentId).orElseThrow(::DataNotFoundException).let {
            if (it.userId != command.userId) throw NotAllowedException()
            return impl.deleteComment(command.copy(targetComment = it))
        }
    }
}