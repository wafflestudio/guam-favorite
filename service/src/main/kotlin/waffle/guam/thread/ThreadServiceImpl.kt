package waffle.guam.thread

import java.time.LocalDateTime
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
import waffle.guam.exception.NotAllowedException
import waffle.guam.exception.DataNotFoundException
import waffle.guam.exception.InvalidRequestException
import waffle.guam.model.Image
import waffle.guam.thread.command.CreateThread
import waffle.guam.thread.command.DeleteThread
import waffle.guam.thread.command.DeleteThreadImage
import waffle.guam.thread.command.EditThreadContent
import waffle.guam.thread.command.RemoveNoticeThread
import waffle.guam.thread.command.SetNoticeThread
import waffle.guam.thread.event.NoticeThreadRemoved
import waffle.guam.thread.event.NoticeThreadSet
import waffle.guam.thread.event.ThreadContentEdited
import waffle.guam.thread.event.ThreadCreated
import waffle.guam.thread.event.ThreadDeleted
import waffle.guam.thread.event.ThreadImageDeleted
import waffle.guam.thread.model.ThreadDetail
import waffle.guam.thread.model.ThreadOverView
import waffle.guam.util.FilterList

@Service
class ThreadServiceImpl(
    private val threadRepository: ThreadRepository,
    private val threadViewRepository: ThreadViewRepository,
    private val commentRepository: CommentRepository,
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository,
    private val imageRepository: ImageRepository,
) : ThreadService  {

    private val nonMemberUserStates = listOf(UserState.GUEST, UserState.QUIT, UserState.DECLINED)

    override fun getThreads(projectId: Long, pageable: Pageable): Page<ThreadOverView> =
        threadViewRepository.findByProjectId(projectId, pageable).map {
            ThreadOverView.of(
                it,
                { threadId -> commentRepository.countByThreadId(threadId) },
                { images -> FilterList.targetImages(images, ImageType.THREAD) },
            )
        }

    override fun getFullThread(threadId: Long): ThreadDetail =
        threadViewRepository.findById(threadId).orElseThrow(::RuntimeException).let {
            ThreadDetail.of(
                it,
                { images -> FilterList.targetImages(images, ImageType.THREAD) },
                { images -> FilterList.targetImages(images, ImageType.COMMENT) },
            )
        }

    @Transactional
    override fun setNoticeThread(command: SetNoticeThread): NoticeThreadSet {
        projectRepository.findById(command.projectId).orElseThrow(::DataNotFoundException).let { project ->
            taskRepository.findByUserIdAndProjectId(command.userId, command.projectId)
                .orElseThrow(::DataNotFoundException).also {
                if (it.userState in nonMemberUserStates) throw NotAllowedException("해당 프로젝트의 공지 쓰레드를 설정할 권한이 없습니다.")
            }
            threadRepository.findById(command.threadId).orElseThrow(::DataNotFoundException).let { thread ->
                projectRepository.save(project.copy(noticeThreadId = thread.id, modifiedAt = LocalDateTime.now()))
                return NoticeThreadSet(thread.id, project.id)
            }
        }
    }

    @Transactional
    override fun removeNoticeThread(command: RemoveNoticeThread): NoticeThreadRemoved {
        projectRepository.findById(command.projectId).orElseThrow(::DataNotFoundException).let { project ->
            taskRepository.findByUserIdAndProjectId(command.userId, project.id).orElseThrow(::DataNotFoundException)
                .also {
                    if (it.userState in nonMemberUserStates) throw NotAllowedException("해당 프로젝트의 공지 쓰레드를 설정할 권한이 없습니다.")
                }
            projectRepository.save(project.copy(noticeThreadId = null, modifiedAt = LocalDateTime.now()))
            return NoticeThreadRemoved(project.id)
        }
    }

    @Transactional
    override fun createThread(command: CreateThread): ThreadCreated {
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
        if (command.content.isNullOrBlank()) {
            threadRepository.save(command.copy(content = null).toEntity())
        } else {
            threadRepository.save(command.copy(content = command.content.trim()).toEntity())
        }.let {
            return ThreadCreated(it.id, command.imageFiles)
        }
        // TODO(event listener : 생성한 쓰레드 ID와 업로드하려는 imageFiles 정보 필요)
        //    if (!command.imageFiles.isNullOrEmpty())
        //        for (imageFile in command.imageFiles)
        //            imageService.upload(imageFile, ImageInfo(threadId, ImageType.THREAD))
    }

    @Transactional
    override fun editThreadContent(command: EditThreadContent): ThreadContentEdited {
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
            return ThreadContentEdited(command.threadId)
        }
    }

    @Transactional
    override fun deleteThreadImage(command: DeleteThreadImage): ThreadImageDeleted {
        imageRepository.findById(command.imageId).orElseThrow(::DataNotFoundException).let { image ->
            threadRepository.findById(command.threadId).orElseThrow(::DataNotFoundException).also {
                if (it.userId != command.userId) throw NotAllowedException()
                if (it.id != image.parentId) throw InvalidRequestException()
                if (it.content.isNullOrBlank()) {
                    if (imageRepository.findByParentIdAndType(it.id, ImageType.THREAD).size < 2)
                        this.deleteThread(DeleteThread(threadId = it.id, userId = command.userId))
                }
                imageRepository.delete(image)
                return ThreadImageDeleted(it.id, image.id)
            }
        }
    }

    @Transactional
    override fun deleteThread(command: DeleteThread): ThreadDeleted {
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
            return ThreadDeleted(command.threadId, it.projectId)
            // TODO(event listener : 삭제한 쓰레드의 id 정보 필요)
            // imageRepository.deleteByParentIdAndType(it.id, ImageType.THREAD)
        }
    }
}