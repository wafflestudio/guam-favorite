package waffle.guam.comment

import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import waffle.guam.comment.command.CreateComment
import waffle.guam.comment.command.DeleteComment
import waffle.guam.comment.command.DeleteCommentImage
import waffle.guam.comment.command.EditCommentContent
import waffle.guam.comment.event.CommentContentEdited
import waffle.guam.comment.event.CommentCreated
import waffle.guam.comment.event.CommentDeleted
import waffle.guam.comment.event.CommentImageDeleted
import waffle.guam.db.entity.UserState
import waffle.guam.db.repository.CommentRepository
import waffle.guam.db.repository.ImageRepository
import waffle.guam.db.repository.ThreadRepository
import waffle.guam.exception.DataNotFoundException
import waffle.guam.exception.InvalidRequestException
import waffle.guam.exception.NotAllowedException
import waffle.guam.task.TaskService
import waffle.guam.task.command.SearchTask

@Primary
@Service
class CommentServicePrimaryImpl(
    private val threadRepository: ThreadRepository,
    private val commentRepository: CommentRepository,
    private val taskService: TaskService,
    private val imageRepository: ImageRepository,
    private val commentServiceImpl: CommentServiceImpl
) : CommentService {
    override fun createComment(command: CreateComment): CommentCreated {
        val parentThread = threadRepository.findById(command.threadId).orElseThrow(::DataNotFoundException)
        val task = taskService.getTasks(SearchTask(listOf(command.userId), listOf(parentThread.projectId))).firstOrNull()
            ?: throw DataNotFoundException()
        if (task.userState == UserState.GUEST) {
            val joinRequestThread = threadRepository.findByUserIdAndProjectId(command.userId, parentThread.projectId).orElseThrow(::DataNotFoundException)
            if (joinRequestThread.id != command.threadId) throw NotAllowedException("아직 다른 쓰레드에 댓글을 생성할 권한이 없습니다.")
        }
        if (task.userState in listOf(UserState.QUIT, UserState.DECLINED)) throw NotAllowedException("해당 프로젝트에 댓글을 생성할 권한이 없습니다.")
        return commentServiceImpl.createComment(command)
    }

    override fun editCommentContent(command: EditCommentContent): CommentContentEdited {
        val currentComment = commentRepository.findById(command.commentId).orElseThrow(::DataNotFoundException)
        if (currentComment.userId != command.userId) throw NotAllowedException()
        if (currentComment.content == command.content) throw InvalidRequestException("수정 전과 동일한 내용입니다.")
        return commentServiceImpl.editCommentContent(command)
    }

    override fun deleteCommentImage(command: DeleteCommentImage): CommentImageDeleted {
        val targetImage = imageRepository.findById(command.imageId).orElseThrow(::DataNotFoundException)
        val parentComment = commentRepository.findById(command.commentId).orElseThrow(::DataNotFoundException)
        if (parentComment.userId != command.userId) throw NotAllowedException()
        if (parentComment.id != targetImage.parentId) throw InvalidRequestException()
        return commentServiceImpl.deleteCommentImage(command)
    }

    override fun deleteComment(command: DeleteComment): CommentDeleted {
        val targetComment = commentRepository.findById(command.commentId).orElseThrow(::DataNotFoundException)
        if (targetComment.userId != command.userId) throw NotAllowedException("타인이 작성한 댓글을 삭제할 수는 없습니다.")
        return commentServiceImpl.deleteComment(command)
    }
}
