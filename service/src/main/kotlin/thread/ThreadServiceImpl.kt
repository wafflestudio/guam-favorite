package waffle.guam.thread

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import waffle.guam.DataNotFoundException
import waffle.guam.InvalidRequestException
import waffle.guam.NotAllowedException
import waffle.guam.comment.CommentRepository
import waffle.guam.image.ImageService
import waffle.guam.image.command.DeleteImages
import waffle.guam.project.ProjectRepository
import waffle.guam.task.TaskService
import waffle.guam.task.query.SearchTask
import waffle.guam.thread.command.CreateJoinThread
import waffle.guam.thread.command.CreateThread
import waffle.guam.thread.command.DeleteThread
import waffle.guam.thread.command.DeleteThreadImage
import waffle.guam.thread.command.EditJoinThreadType
import waffle.guam.thread.command.EditThreadContent
import waffle.guam.thread.command.SetNoticeThread
import waffle.guam.thread.event.JoinThreadCreated
import waffle.guam.thread.event.JoinThreadTypeEdited
import waffle.guam.thread.event.NoticeThreadSet
import waffle.guam.thread.event.ThreadContentEdited
import waffle.guam.thread.event.ThreadCreated
import waffle.guam.thread.event.ThreadDeleted
import waffle.guam.thread.event.ThreadImageDeleted
import waffle.guam.thread.model.ThreadDetail
import waffle.guam.thread.model.ThreadOverView
import waffle.guam.thread.model.ThreadType
import java.time.Instant

@Service
class ThreadServiceImpl(
    private val threadRepository: ThreadRepository,
    private val threadViewRepository: ThreadViewRepository,
    private val projectRepository: ProjectRepository,
    private val taskService: TaskService,
    private val commentRepository: CommentRepository,
    private val imageService: ImageService,
) : ThreadService {

    override fun getThreads(projectId: Long, pageable: Pageable): Page<ThreadOverView> =
        threadViewRepository.findByProjectId(projectId, pageable).map {
            ThreadOverView.of(it) { threadId -> commentRepository.countByThreadId(threadId) }
        }

    override fun getFullThread(threadId: Long): ThreadDetail =
        threadViewRepository.findById(threadId).orElseThrow(::DataNotFoundException).let {
            ThreadDetail.of(it)
        }

    fun getThreadOverView(threadId: Long): ThreadOverView =
        threadViewRepository.findById(threadId).orElseThrow(::DataNotFoundException).let {
            ThreadOverView.of(it) { threadId -> commentRepository.countByThreadId(threadId) }
        }

    @Transactional
    override fun setNoticeThread(command: SetNoticeThread): NoticeThreadSet {
        val project = projectRepository.findById(command.projectId).orElseThrow(::DataNotFoundException)

        if (!taskService.isMemberOrLeader(projectId = command.projectId, userId = command.userId)) {
            throw NotAllowedException("해당 프로젝트의 공지 쓰레드를 설정할 권한이 없습니다.")
        }

        if (command.threadId == null) {
            projectRepository.save(project.copy(noticeThreadId = null, modifiedAt = Instant.now()))
            return NoticeThreadSet(projectId = project.id)
        }

        threadRepository.findById(command.threadId).orElseThrow(::DataNotFoundException).let {
            projectRepository.save(project.copy(noticeThreadId = it.id, modifiedAt = Instant.now()))
            return NoticeThreadSet(projectId = project.id, threadId = it.id)
        }
    }

    @Transactional
    override fun createThread(command: CreateThread): ThreadCreated {
        val parentProject = projectRepository.findById(command.projectId).orElseThrow(::DataNotFoundException)

        if (!taskService.isMemberOrLeader(command.projectId, command.userId)) {
            throw NotAllowedException("해당 프로젝트에 쓰레드를 생성할 권한이 없습니다.")
        }

        val task = taskService.getTask(SearchTask.taskQuery().userIds(command.userId).projectIds(command.projectId))

        threadRepository.save(command.copy(content = command.content?.trim()).toEntity()).let {
            return ThreadCreated.of(project = parentProject, threadId = it.id, command = command, task = task)
        }
    }

    @Transactional
    override fun createJoinThread(command: CreateJoinThread): JoinThreadCreated =
        threadRepository.save(command.copy(content = command.content).toEntity()).let {
            return JoinThreadCreated(it.id)
        }

    @Transactional
    override fun editThreadContent(command: EditThreadContent): ThreadContentEdited =
        threadRepository.findById(command.threadId).orElseThrow(::DataNotFoundException).let {
            if (it.userId != command.userId) {
                throw NotAllowedException("타인의 쓰레드를 수정할 수는 없습니다.")
            }
            if (it.content == command.content) {
                throw InvalidRequestException("수정 전과 동일한 내용입니다.")
            }
            if (command.content.isEmpty() && it.type != ThreadType.NORMAL.name) {
                throw InvalidRequestException("참가 신청서는 내용이 비어있을 수 없습니다.")
            }

            val editedThread =
                threadRepository.save(it.copy(content = command.content.trim(), modifiedAt = Instant.now()))
            return ThreadContentEdited(it.id, editedThread.content)
        }

    @Transactional
    override fun editJoinThreadType(command: EditJoinThreadType): JoinThreadTypeEdited =
        threadRepository.findByUserIdAndProjectId(command.userId, command.projectId)
            .orElseThrow(::DataNotFoundException).let {
                threadRepository.save(it.copy(type = command.type.name, modifiedAt = Instant.now()))
                return JoinThreadTypeEdited(it.id, command.type)
            }

    @Transactional
    override fun deleteThreadImage(command: DeleteThreadImage): ThreadImageDeleted =
        threadRepository.findById(command.threadId).orElseThrow(::DataNotFoundException).let {
            if (it.userId != command.userId) {
                throw NotAllowedException("타인이 업로드한 이미지를 삭제할 수는 없습니다.")
            }

            imageService.deleteImages(DeleteImages.ById(listOf(command.imageId)))

            return ThreadImageDeleted(threadId = it.id, deletedImageId = command.imageId, projectId = it.projectId)
        }

    @Transactional
    override fun deleteThread(command: DeleteThread): ThreadDeleted =
        threadRepository.findById(command.threadId).orElseThrow(::DataNotFoundException).let {
            if (it.type != ThreadType.NORMAL.name) {
                throw NotAllowedException("해당 쓰레드는 삭제할 수 없습니다.")
            }

            if (it.userId != command.userId) {
                throw NotAllowedException("타인이 작성한 쓰레드를 삭제할 수는 없습니다.")
            }

            if (!taskService.isMemberOrLeader(projectId = it.projectId, userId = command.userId)) {
                throw NotAllowedException("해당 프로젝트의 쓰레드를 삭제할 권한이 없습니다.")
            }

            when (commentRepository.existsByThreadId(command.threadId)) {
                true -> threadRepository.save(it.copy(content = "", modifiedAt = Instant.now()))
                false -> threadRepository.delete(it)
            }
            return ThreadDeleted(threadId = it.id, projectId = it.projectId)
        }
}
