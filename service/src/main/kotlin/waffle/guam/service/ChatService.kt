package waffle.guam.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import waffle.guam.db.entity.CommentEntity
import waffle.guam.db.entity.ImageType
import waffle.guam.db.entity.State
import waffle.guam.db.repository.CommentRepository
import waffle.guam.db.repository.ImageRepository
import waffle.guam.db.repository.ProjectRepository
import waffle.guam.db.repository.TaskRepository
import waffle.guam.db.repository.ThreadRepository
import waffle.guam.db.repository.ThreadViewRepository
import waffle.guam.exception.DataNotFoundException
import waffle.guam.exception.InvalidRequestException
import waffle.guam.exception.NotAllowedException
import waffle.guam.model.Image
import waffle.guam.model.ThreadDetail
import waffle.guam.model.ThreadOverView
import waffle.guam.service.command.CreateComment
import waffle.guam.service.command.CreateThread
import waffle.guam.service.command.DeleteComment
import waffle.guam.service.command.DeleteCommentImage
import waffle.guam.service.command.DeleteThread
import waffle.guam.service.command.DeleteThreadImage
import waffle.guam.service.command.EditCommentContent
import waffle.guam.service.command.EditThreadContent
import waffle.guam.service.command.SetNoticeThread
import java.time.LocalDateTime

@Service
class ChatService(
    private val threadRepository: ThreadRepository,
    private val threadViewRepository: ThreadViewRepository,
    private val commentRepository: CommentRepository,
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository,
    private val imageRepository: ImageRepository,
    private val imageService: ImageService
) {
    fun getThreads(projectId: Long, pageable: Pageable): Page<ThreadOverView> {
        return threadViewRepository.findByProjectId(projectId, pageable).map {
            ThreadOverView.of(
                it,
                { threadId -> commentRepository.countByThreadId(threadId) },
                { images ->
                    images.filter { allImage -> allImage.type == ImageType.THREAD }
                        .map { threadImage -> Image.of(threadImage) }
                }
            )
        }
    }

    fun getFullThread(threadId: Long): ThreadDetail {
        return threadViewRepository.findById(threadId).orElseThrow(::RuntimeException).let {
            ThreadDetail.of(
                it,
                { images ->
                    images.filter { allImage -> allImage.type == ImageType.THREAD }
                        .map { threadImage -> Image.of(threadImage) }
                },
                { images ->
                    images.filter { allImage -> allImage.type == ImageType.COMMENT }
                        .map { commentImage -> Image.of(commentImage) }
                },
            )
        }
    }

    @Transactional
    fun setNoticeThread(command: SetNoticeThread): Boolean {
        projectRepository.findById(command.projectId).orElseThrow(::DataNotFoundException).let { project ->
            taskRepository.findByUserIdAndProjectId(command.userId, command.projectId).orElseThrow(::NotAllowedException).also {
                if (it.state == State.GUEST) throw NotAllowedException()
            }
            threadRepository.findById(command.threadId).orElseThrow(::DataNotFoundException).let { thread ->
                projectRepository.save(project.copy(noticeThreadId = thread.id, modifiedAt = LocalDateTime.now()))
            }
        }
        return true
    }

    @Transactional
    fun createThread(command: CreateThread): Boolean {
        if (command.content.isNullOrBlank() && command.imageFiles.isNullOrEmpty()) throw InvalidRequestException("입력된 내용이 없습니다.")
        projectRepository.findById(command.projectId).orElseThrow(::DataNotFoundException)
        val threadId = threadRepository.save(command.toEntity()).id
        if (!command.imageFiles.isNullOrEmpty())
            for (imageFile in command.imageFiles)
                imageService.upload(imageFile, ImageInfo(threadId, ImageType.THREAD))
        return true
    }

    @Transactional
    fun editThreadContent(command: EditThreadContent): Boolean {
        threadRepository.findById(command.threadId).orElseThrow(::DataNotFoundException).let {
            if (it.userId != command.userId) throw NotAllowedException()
            if (it.content == command.content) throw InvalidRequestException("수정 전과 동일한 내용입니다.")
            threadRepository.save(it.copy(content = command.content, modifiedAt = LocalDateTime.now()))
        }
        return true
    }

    @Transactional
    fun deleteThreadImage(command: DeleteThreadImage): Boolean {
        imageRepository.findById(command.imageId).orElseThrow(::DataNotFoundException).let { image ->
            threadRepository.findById(command.threadId).orElseThrow(::DataNotFoundException).also {
                if (it.userId != command.userId) throw NotAllowedException()
                if (it.id != image.parentId) throw InvalidRequestException()
            }
            imageRepository.delete(image)
        }
        return true
    }

    @Transactional
    fun deleteThread(command: DeleteThread): Boolean {
        threadRepository.findById(command.threadId).orElseThrow(::DataNotFoundException).let { thread ->
            if (thread.userId != command.userId) throw NotAllowedException()
            val childComments: List<CommentEntity> = commentRepository.findByThreadId(command.threadId)
            if (childComments.isNotEmpty()) {
                imageRepository.deleteByParentIdInAndType(childComments.map { it.id }, ImageType.COMMENT)
                commentRepository.deleteAll(childComments)
            }
            imageRepository.deleteByParentIdAndType(thread.id, ImageType.THREAD)
            threadRepository.delete(thread)
        }
        return true
    }

    @Transactional
    fun createComment(command: CreateComment): Boolean {
        if (command.content.isNullOrBlank() && command.imageFiles.isNullOrEmpty()) throw InvalidRequestException("입력된 내용이 없습니다.")
        threadRepository.findById(command.threadId).orElseThrow(::DataNotFoundException)
        val commentId = commentRepository.save(command.toEntity()).id
        if (!command.imageFiles.isNullOrEmpty())
            for (imageFile in command.imageFiles)
                imageService.upload(imageFile, ImageInfo(commentId, ImageType.COMMENT))
        return true
    }

    @Transactional
    fun editCommentContent(command: EditCommentContent): Boolean {
        commentRepository.findById(command.commentId).orElseThrow(::DataNotFoundException).let {
            if (it.userId != command.userId) throw NotAllowedException()
            if (it.content == command.content) throw InvalidRequestException("수정 전과 동일한 내용입니다.")
            commentRepository.save(it.copy(content = command.content, modifiedAt = LocalDateTime.now()))
        }
        return true
    }

    @Transactional
    fun deleteCommentImage(command: DeleteCommentImage): Boolean {
        imageRepository.findById(command.imageId).orElseThrow(::DataNotFoundException).let { image ->
            commentRepository.findById(command.commentId).orElseThrow(::DataNotFoundException).also {
                if (it.userId != command.userId) throw NotAllowedException()
                if (it.id != image.parentId) throw InvalidRequestException()
            }
            imageRepository.delete(image)
        }
        return true
    }

    @Transactional
    fun deleteComment(command: DeleteComment): Boolean {
        commentRepository.findById(command.commentId).orElseThrow(::DataNotFoundException).let {
            if (it.userId != command.userId) throw NotAllowedException()
            imageRepository.deleteByParentIdAndType(command.commentId, ImageType.COMMENT)
            commentRepository.delete(it)
        }
        return true
    }
}
