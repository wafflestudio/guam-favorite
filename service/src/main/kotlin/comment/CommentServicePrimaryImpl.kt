package waffle.guam.comment

import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import waffle.guam.DataNotFoundException
import waffle.guam.InvalidRequestException
import waffle.guam.NotAllowedException
import waffle.guam.comment.command.CreateComment
import waffle.guam.comment.command.DeleteComment
import waffle.guam.comment.command.DeleteCommentImage
import waffle.guam.comment.command.EditCommentContent
import waffle.guam.comment.event.CommentContentEdited
import waffle.guam.comment.event.CommentCreated
import waffle.guam.comment.event.CommentDeleted
import waffle.guam.comment.event.CommentImageDeleted
import waffle.guam.task.TaskService
import waffle.guam.task.command.SearchTask
import waffle.guam.task.model.UserState
import waffle.guam.thread.ThreadService
import waffle.guam.thread.command.GetThreadInfo

@Primary
@Service
class CommentServicePrimaryImpl(
    private val commentRepository: CommentRepository,
    private val commentServiceImpl: CommentServiceImpl,
    private val threadService: ThreadService,
    private val taskService: TaskService
) : CommentService {
    override fun createComment(command: CreateComment): CommentCreated {
        val parentThread = threadService.getThreadInfo(GetThreadInfo.ByIds(listOf(command.threadId)))
            .firstOrNull() ?: throw DataNotFoundException()
        val task = taskService.getTasks(SearchTask(listOf(command.userId), listOf(parentThread.projectId)))
            .firstOrNull() ?: throw DataNotFoundException()
        if (task.userState == UserState.GUEST) {
            val joinRequestThread = threadService.getThreadInfo(
                GetThreadInfo.ByUserIdAndProjectId(command.userId, parentThread.projectId)
            ).firstOrNull() ?: throw DataNotFoundException()
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
        val parentComment = commentRepository.findById(command.commentId).orElseThrow(::DataNotFoundException)
        if (parentComment.userId != command.userId) throw NotAllowedException()
        return commentServiceImpl.deleteCommentImage(command)
    }

    override fun deleteComment(command: DeleteComment): CommentDeleted {
        val targetComment = commentRepository.findById(command.commentId).orElseThrow(::DataNotFoundException)
        if (targetComment.userId != command.userId) throw NotAllowedException("타인이 작성한 댓글을 삭제할 수는 없습니다.")
        return commentServiceImpl.deleteComment(command)
    }
}
