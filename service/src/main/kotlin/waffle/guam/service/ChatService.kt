package waffle.guam.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import waffle.guam.db.entity.ImageType
import waffle.guam.db.entity.UserState
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
    private val nonMemberUserStates = listOf(UserState.GUEST, UserState.QUIT, UserState.DECLINED)

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
            taskRepository.findByUserIdAndProjectId(command.userId, command.projectId).orElseThrow(::DataNotFoundException).also {
                if (it.userState in nonMemberUserStates) throw NotAllowedException("해당 프로젝트의 공지 쓰레드를 설정할 권한이 없습니다.")
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
            taskRepository.findByUserIdAndProjectId(command.userId, command.projectId).orElseThrow(::DataNotFoundException).also {
                if (it.userState in nonMemberUserStates) throw NotAllowedException("해당 프로젝트의 공지 쓰레드를 설정할 권한이 없습니다.")
            }
            projectRepository.save(project.copy(noticeThreadId = null, modifiedAt = LocalDateTime.now()))
        }
        return true
    }

    @Transactional
    fun createThread(command: CreateThread): Boolean {
        projectRepository.findById(command.projectId).orElseThrow(::DataNotFoundException)
        taskRepository.findByUserIdAndProjectId(command.userId, command.projectId)
            .orElseThrow(::DataNotFoundException).let {
                if (it.userState in nonMemberUserStates)
                    if (it.userState == UserState.GUEST) {
                        if (threadRepository.countByUserIdAndProjectId(command.userId, command.projectId) > 0)
                            throw NotAllowedException("아직 새로운 쓰레드를 생성할 권한이 없습니다.")
                    } else {
                        throw NotAllowedException("해당 프로젝트에 쓰레드를 생성할 권한이 없습니다.")
                    }
            }
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
                }
                imageRepository.delete(image)
            }
        }
        return true
    }

    @Transactional
    fun deleteThread(command: DeleteThread): Boolean {
        threadRepository.findById(command.threadId).orElseThrow(::DataNotFoundException).let {
            if (it.userId != command.userId) throw NotAllowedException("타인이 작성한 쓰레드를 삭제할 수는 없습니다.")
            taskRepository.findByUserIdAndProjectId(command.userId, it.projectId)
                .orElseThrow(::DataNotFoundException).let { task ->
                    if (task.userState in nonMemberUserStates) throw NotAllowedException("해당 프로젝트의 쓰레드를 삭제할 권한이 없습니다.")
                }
            if (commentRepository.findByThreadId(command.threadId).isEmpty()) {
                threadRepository.delete(it)
            } else {
                threadRepository.save(it.copy(content = null))
            }
            imageRepository.deleteByParentIdAndType(it.id, ImageType.THREAD)
        }
        return true
    }

    @Transactional
    fun createComment(command: CreateComment): Boolean {
        threadRepository.findById(command.threadId).orElseThrow(::DataNotFoundException).let { parentThread ->
            taskRepository.findByUserIdAndProjectId(command.userId, parentThread.projectId)
                .orElseThrow(::DataNotFoundException).let {
                    if (it.userState in nonMemberUserStates)
                        if (it.userState == UserState.GUEST) {
                            if (threadRepository.findByUserIdAndProjectId(command.userId, parentThread.projectId)
                                .orElseThrow(::DataNotFoundException).id != command.threadId
                            )
                                throw NotAllowedException("아직 다른 쓰레드에 댓글을 생성할 권한이 없습니다.")
                        } else {
                            throw NotAllowedException("해당 프로젝트에 댓글을 생성할 권한이 없습니다.")
                        }
                }
        }
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
                }
                imageRepository.delete(image)
            }
        }
        return true
    }

    @Transactional
    fun deleteComment(command: DeleteComment): Boolean {
        commentRepository.findById(command.commentId).orElseThrow(::DataNotFoundException).let {
            if (it.userId != command.userId) throw NotAllowedException("타인이 작성한 댓글을 삭제할 수는 없습니다.")
            imageRepository.deleteByParentIdAndType(command.commentId, ImageType.COMMENT)
            commentRepository.delete(it)

            if (commentRepository.findByThreadId(it.threadId).isEmpty())
                if (threadRepository.findById(it.threadId).get().content.isNullOrBlank())
                    if (imageRepository.findByParentIdAndType(it.threadId, ImageType.THREAD).isEmpty())
                        threadRepository.deleteById(it.threadId)
        }
        return true
    }
}
