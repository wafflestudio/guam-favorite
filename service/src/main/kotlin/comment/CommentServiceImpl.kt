package waffle.guam.comment

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import waffle.guam.DataNotFoundException
import waffle.guam.InvalidRequestException
import waffle.guam.NotAllowedException
import waffle.guam.comment.command.CreateComment
import waffle.guam.comment.command.DeleteComment
import waffle.guam.comment.command.EditCommentContent
import waffle.guam.comment.event.CommentContentEdited
import waffle.guam.comment.event.CommentCreated
import waffle.guam.comment.event.CommentDeleted
import waffle.guam.comment.model.Comment
import waffle.guam.task.TaskService
import waffle.guam.task.model.UserState
import waffle.guam.task.query.SearchTask.Companion.taskQuery
import waffle.guam.thread.ThreadRepository
import waffle.guam.user.UserRepository
import java.time.Instant

@Service
class CommentServiceImpl(
    private val commentRepository: CommentRepository,
    private val threadRepository: ThreadRepository,
    private val taskService: TaskService,
    private val userRepository: UserRepository
) : CommentService {

    override fun getComment(commentId: Long): Comment =
        commentRepository.findById(commentId).orElseThrow(::DataNotFoundException).let {
            Comment.of(it)
        }

    @Transactional
    override fun createComment(command: CreateComment): CommentCreated {
        val parentThread = threadRepository.findById(command.threadId).orElseThrow(::DataNotFoundException)

        validateCommentCreator(command, parentThread.projectId)

        val user = userRepository.findById(command.userId).orElseThrow(::DataNotFoundException)

        commentRepository.save(command.copy(content = command.content?.trim()).toEntity(user)).let {
            return CommentCreated.of(thread = parentThread, commentId = it.id, command = command, creator = user)
        }
    }

    @Transactional
    override fun editCommentContent(command: EditCommentContent): CommentContentEdited =
        commentRepository.findById(command.commentId).orElseThrow(::DataNotFoundException).let {
            if (it.user.id != command.userId) {
                throw NotAllowedException("타인의 댓글을 수정할 수는 없습니다.")
            }
            if (it.content == command.content) {
                throw InvalidRequestException("수정 전과 동일한 내용입니다.")
            }

            val editedComment = commentRepository.save(it.copy(content = command.content.trim(), modifiedAt = Instant.now()))

            return CommentContentEdited(it.id, editedComment.content, it.images, it.threadId)
        }

    @Transactional
    override fun deleteComment(command: DeleteComment): CommentDeleted =
        commentRepository.findById(command.commentId).orElseThrow(::DataNotFoundException).let {
            if (it.user.id != command.userId) {
                throw NotAllowedException("타인이 작성한 댓글을 삭제할 수는 없습니다.")
            }

            commentRepository.delete(it)
            return CommentDeleted(commentId = command.commentId, threadId = it.threadId)
        }

    protected fun validateCommentCreator(command: CreateComment, projectId: Long) {
        val task = taskService.getTask(taskQuery().userIds(command.userId).projectIds(projectId))

        if (task.userState == UserState.GUEST) {
            val joinRequestThread = threadRepository.findByUserIdAndProjectId(command.userId, projectId)
                .orElseThrow(::DataNotFoundException)
            if (joinRequestThread.id != command.threadId) {
                throw NotAllowedException("아직 다른 쓰레드에 댓글을 생성할 권한이 없습니다.")
            }
        }
        if (task.userState !in UserState.validStates) {
            throw NotAllowedException("해당 프로젝트에 댓글을 생성할 권한이 없습니다.")
        }
    }
}
