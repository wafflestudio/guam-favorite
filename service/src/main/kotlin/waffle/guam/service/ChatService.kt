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
import waffle.guam.service.command.RemoveNoticeThread
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
    fun getThreads(projectId: Long, pageable: Pageable): Page<ThreadOverView> =
        threadViewRepository.findByProjectId(projectId, pageable).map {
            ThreadOverView.of(
                it,
                { threadId -> commentRepository.countByThreadId(threadId) },
                { images ->
                    images.filter { allImage -> allImage.type == ImageType.THREAD }
                        .map { threadImage -> Image.of(threadImage) }
                }
            )
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
    fun removeNoticeThread(command: RemoveNoticeThread): Boolean {
        projectRepository.findById(command.projectId).orElseThrow(::DataNotFoundException).let { project ->
            taskRepository.findByUserIdAndProjectId(command.userId, command.projectId).orElseThrow(::NotAllowedException).also {
                if (it.state == State.GUEST) throw NotAllowedException()
            }
            projectRepository.save(project.copy(noticeThreadId = null, modifiedAt = LocalDateTime.now()))
        }
        return true
    }

    @Transactional
    fun createThread(command: CreateThread): Boolean {
        projectRepository.findById(command.projectId).orElseThrow(::DataNotFoundException)
        val threadId = if (command.content.isNullOrBlank()) {
            threadRepository.save(command.copy(content = null).toEntity()).id
        } else {
            threadRepository.save(command.copy(content = command.content.trim()).toEntity()).id
        }
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
            if (command.content.isBlank()) {
                if (imageRepository.findByParentIdAndType(it.id, ImageType.THREAD).isEmpty()) {
                    this.deleteThread(DeleteThread(threadId = it.id, userId = command.userId))
                } else {
                    threadRepository.save(it.copy(content = null, modifiedAt = LocalDateTime.now()))
                }
            } else {
                threadRepository.save(it.copy(content = command.content.trim(), modifiedAt = LocalDateTime.now()))
            }
        }
        return true
    }

    @Transactional
    fun deleteThreadImage(command: DeleteThreadImage): Boolean {
        imageRepository.findById(command.imageId).orElseThrow(::DataNotFoundException).let { image ->
            threadRepository.findById(command.threadId).orElseThrow(::DataNotFoundException).also {
                if (it.userId != command.userId) throw NotAllowedException()
                if (it.id != image.parentId) throw InvalidRequestException()
                if (it.content.isNullOrBlank()) {
                    if (imageRepository.findByParentIdAndType(it.id, ImageType.THREAD).size < 2)
                        this.deleteThread(DeleteThread(threadId = it.id, userId = command.userId))
                } else {
                    imageRepository.delete(image)
                }
            }
        }
        return true
    }

    @Transactional
    fun deleteThread(command: DeleteThread): Boolean {
        threadRepository.findById(command.threadId).orElseThrow(::DataNotFoundException).let {
            if (it.userId != command.userId) throw NotAllowedException()
            val childComments: List<CommentEntity> = commentRepository.findByThreadId(command.threadId)
            imageRepository.deleteByParentIdAndType(it.id, ImageType.THREAD)
            if (childComments.isEmpty()) {
                threadRepository.delete(it)
            } else {
                threadRepository.save(it.copy(content = null))
            }
        }
        return true
    }

    @Transactional
    fun createComment(command: CreateComment): Boolean {
        threadRepository.findById(command.threadId).orElseThrow(::DataNotFoundException)
        val commentId = if (command.content.isNullOrBlank()) {
            commentRepository.save(command.copy(content = null).toEntity()).id
        } else {
            commentRepository.save(command.copy(content = command.content.trim()).toEntity()).id
        }
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

            if (command.content.isBlank()) {
                if (imageRepository.findByParentIdAndType(it.id, ImageType.COMMENT).isEmpty()) {
                    this.deleteComment((DeleteComment(commentId = it.id, userId = command.userId)))
                } else {
                    commentRepository.save(it.copy(content = null, modifiedAt = LocalDateTime.now()))
                }
            } else {
                commentRepository.save(it.copy(content = command.content.trim(), modifiedAt = LocalDateTime.now()))
            }
        }
        return true
    }

    @Transactional
    fun deleteCommentImage(command: DeleteCommentImage): Boolean {
        imageRepository.findById(command.imageId).orElseThrow(::DataNotFoundException).let { image ->
            commentRepository.findById(command.commentId).orElseThrow(::DataNotFoundException).also {
                if (it.userId != command.userId) throw NotAllowedException()
                if (it.id != image.parentId) throw InvalidRequestException()

                if (it.content.isNullOrBlank()) {
                    if (imageRepository.findByParentIdAndType(it.id, ImageType.COMMENT).size < 2)
                        commentRepository.delete(it)
                } else {
                    imageRepository.delete(image)
                }
            }
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

