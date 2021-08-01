package waffle.guam.comment

import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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
import waffle.guam.db.entity.UserState
import waffle.guam.db.repository.TaskRepository
import waffle.guam.exception.DataNotFoundException

@Primary
@Service
class CommentServiceImplPrimary(
    private val threadRepository: ThreadRepository,
    private val commentRepository: CommentRepository,
    private val taskRepository: TaskRepository,
    private val imageRepository: ImageRepository,
    private val impl: CommentServiceImpl
) : CommentService {

    private val nonMemberUserStates = listOf(UserState.GUEST, UserState.QUIT, UserState.DECLINED)

    @Transactional
    override fun createComment(command: CreateComment): CommentCreated {
        threadRepository.findById(command.threadId).orElseThrow(::DataNotFoundException).let { parentThread ->
            taskRepository.findByUserIdAndProjectId(command.userId, parentThread.projectId)
                .orElseThrow(::DataNotFoundException).let {
                    if (it.userState in nonMemberUserStates)
                        if (it.userState == UserState.GUEST) {
//   TODO([GUAM-94] 수정 사항 - findByUserIdAndProjectId 메서드 때문에 컨플릭 날까봐 일단 주석처리)
//                            if (threadRepository.findByUserIdAndProjectId(command.userId, parentThread.projectId)
//                                    .orElseThrow(::DataNotFoundException).id != command.threadId
//                            )
//                                throw NotAllowedException("아직 다른 쓰레드에 댓글을 생성할 권한이 없습니다.")
                        } else {
                            throw NotAllowedException("해당 프로젝트에 댓글을 생성할 권한이 없습니다.")
                        }
                }
        }
        return impl.createComment(command)
    }

    @Transactional
    override fun editCommentContent(command: EditCommentContent): Boolean {
        commentRepository.findById(command.commentId).orElseThrow(::DataNotFoundException).let {
            if (it.userId != command.userId) throw NotAllowedException()
            if (it.content == command.content) throw InvalidRequestException("수정 전과 동일한 내용입니다.")
            return impl.editCommentContent(command)
        }
    }

    @Transactional
    override fun deleteCommentImage(command: DeleteCommentImage): Boolean {
        imageRepository.findById(command.imageId).orElseThrow(::DataNotFoundException).let { image ->
            commentRepository.findById(command.commentId).orElseThrow(::DataNotFoundException).also {
                if (it.userId != command.userId) throw NotAllowedException()
                if (it.id != image.parentId) throw InvalidRequestException()
                command.commentContent = it.content
                return impl.deleteCommentImage(command)
            }
        }
    }

    @Transactional
    override fun deleteComment(command: DeleteComment): CommentDeleted {
        commentRepository.findById(command.commentId).orElseThrow(::DataNotFoundException).let {
            if (it.userId != command.userId) throw NotAllowedException("타인이 작성한 댓글을 삭제할 수는 없습니다.")
            command.threadId = it.threadId
            return impl.deleteComment(command)
        }
    }
}
