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
import waffle.guam.db.entity.UserState
import waffle.guam.db.repository.CommentRepository
import waffle.guam.db.repository.ImageRepository
import waffle.guam.db.repository.TaskRepository
import waffle.guam.db.repository.ThreadRepository
import waffle.guam.exception.DataNotFoundException
import waffle.guam.exception.InvalidRequestException
import waffle.guam.exception.NotAllowedException
import java.time.LocalDateTime

@Service
class CommentServiceImpl(
    private val threadRepository: ThreadRepository,
    private val commentRepository: CommentRepository,
    private val taskRepository: TaskRepository,
    private val imageRepository: ImageRepository,
) : CommentService {

    @Transactional
    override fun createComment(command: CreateComment): CommentCreated {
        threadRepository.findById(command.threadId).orElseThrow(::DataNotFoundException).let { parentThread ->
            taskRepository.findByUserIdAndProjectId(command.userId, parentThread.projectId).orElseThrow(::DataNotFoundException).let {
                if (it.userState == UserState.GUEST)
                    if (threadRepository.findByUserIdAndProjectId(command.userId, parentThread.projectId)
                        .orElseThrow(::DataNotFoundException).id != command.threadId
                    )
                        throw NotAllowedException("아직 다른 쓰레드에 댓글을 생성할 권한이 없습니다.")
                if (it.userState == UserState.QUIT || it.userState == UserState.DECLINED)
                    throw NotAllowedException("해당 프로젝트에 댓글을 생성할 권한이 없습니다.")
            }
        }
        if (command.content.isNullOrBlank()) {
            commentRepository.save(command.copy(content = null).toEntity())
        } else {
            commentRepository.save(command.copy(content = command.content.trim()).toEntity())
        }.let {
            return CommentCreated(it.id, command.imageFiles)
        }
        //   TODO(event listener : 생성한 댓글 ID와 업로드하려는 imageFiles 정보 필요)
        //    if (!command.imageFiles.isNullOrEmpty())
        //        for (imageFile in command.imageFiles)
        //            imageService.upload(imageFile, ImageInfo(commentId, ImageType.COMMENT))
    }

    @Transactional
    override fun editCommentContent(command: EditCommentContent): CommentContentEdited {
        commentRepository.findById(command.commentId).orElseThrow(::DataNotFoundException).let {
            if (it.userId != command.userId) throw NotAllowedException()
            if (it.content == command.content) throw InvalidRequestException("수정 전과 동일한 내용입니다.")
            if (command.content.isNotBlank()) {
                commentRepository.save(it.copy(content = command.content.trim(), modifiedAt = LocalDateTime.now()))
                return CommentContentEdited(it.id)
            }
            if (imageRepository.findByParentIdAndType(command.commentId, ImageType.COMMENT).isNotEmpty()) {
                commentRepository.save(it.copy(content = null, modifiedAt = LocalDateTime.now()))
                return CommentContentEdited(it.id)
            }
            this.deleteComment(DeleteComment(commentId = command.commentId, userId = command.userId))
            return CommentContentEdited(it.id)
        }
    }

    @Transactional
    override fun deleteCommentImage(command: DeleteCommentImage): CommentImageDeleted {
        imageRepository.findById(command.imageId).orElseThrow(::DataNotFoundException).let { image ->
            commentRepository.findById(command.commentId).orElseThrow(::DataNotFoundException).also {
                if (it.userId != command.userId) throw NotAllowedException()
                if (it.id != image.parentId) throw InvalidRequestException()
                if (it.content.isNullOrBlank()) {
                    if (imageRepository.findByParentIdAndType(it.id, ImageType.COMMENT).size < 2)
                        this.deleteComment(DeleteComment(commentId = command.commentId, userId = command.userId))
                }
                imageRepository.deleteById(image.id)
                return CommentImageDeleted(it.id, image.id)
                // TODO(event listener : 빈 쓰레드가 되는 경우 쓰레드 자동 삭제? - deleteComment 호출하는 경우 불필요)
            }
        }
    }

    @Transactional
    override fun deleteComment(command: DeleteComment): CommentDeleted {
        commentRepository.findById(command.commentId).orElseThrow(::DataNotFoundException).let {
            if (it.userId != command.userId) throw NotAllowedException("타인이 작성한 댓글을 삭제할 수는 없습니다.")
            commentRepository.delete(it)
            return CommentDeleted(command.commentId, it.threadId)
            //   TODO(event listener : 삭제한 댓글의 id & threadId 정보 필요)
            //    imageRepository.deleteByParentIdAndType(command.commentId, ImageType.COMMENT)
            //    if (commentRepository.findByThreadId(it.threadId).isEmpty())
            //        if (threadRepository.findById(it.threadId).get().content.isNullOrBlank())
            //            if (imageRepository.findByParentIdAndType(it.threadId, ImageType.THREAD).isEmpty())
            //                threadRepository.deleteById(it.threadId)
        }
    }
}
