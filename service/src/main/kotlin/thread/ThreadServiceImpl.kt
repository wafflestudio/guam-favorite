package waffle.guam.thread

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import waffle.guam.DataNotFoundException
import waffle.guam.InvalidRequestException
import waffle.guam.NotAllowedException
import waffle.guam.comment.CommentRepository
import waffle.guam.project.ProjectRepository
import waffle.guam.task.TaskService
import waffle.guam.task.query.SearchTask
import waffle.guam.thread.command.CreateJoinRequestThread
import waffle.guam.thread.command.CreateThread
import waffle.guam.thread.command.DeleteThread
import waffle.guam.thread.command.EditThreadContent
import waffle.guam.thread.command.SetNoticeThread
import waffle.guam.thread.event.JoinRequestThreadCreated
import waffle.guam.thread.event.NoticeThreadSet
import waffle.guam.thread.event.ThreadContentEdited
import waffle.guam.thread.event.ThreadCreated
import waffle.guam.thread.event.ThreadDeleted
import waffle.guam.thread.model.ThreadDetail
import waffle.guam.thread.model.ThreadInfo
import waffle.guam.thread.model.ThreadOverView
import java.time.Instant

@Service
class ThreadServiceImpl(
    private val threadRepository: ThreadRepository,
    private val threadViewRepository: ThreadViewRepository,
    private val projectRepository: ProjectRepository,
    private val taskService: TaskService,
    private val commentRepository: CommentRepository,
) : ThreadService {

    override fun getThread(threadId: Long): ThreadInfo =
        threadRepository.findById(threadId).orElseThrow(::DataNotFoundException).let {
            ThreadInfo.of(it)
        }

    override fun getThreads(projectId: Long, pageable: Pageable): Page<ThreadOverView> =
        threadViewRepository.findByProjectId(projectId, pageable).map {
            ThreadOverView.of(it) { threadId -> commentRepository.countByThreadId(threadId) }
        }

    override fun getFullThread(threadId: Long): ThreadDetail =
        threadViewRepository.findById(threadId).orElseThrow(::DataNotFoundException).let {
            ThreadDetail.of(it)
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

        val task = taskService.getTask(SearchTask.taskQuery().userIds(command.userId).projectIds(command.projectId))

        threadRepository.save(command.copy(content = command.content?.trim()).toEntity()).let {
            return ThreadCreated.of(project = parentProject, threadId = it.id, command = command, task = task)
        }
    }

    @Transactional
    override fun createJoinRequestThread(command: CreateJoinRequestThread): JoinRequestThreadCreated =
        threadRepository.save(command.copy(content = command.content).toEntity()).let {
            return JoinRequestThreadCreated(it.id)
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

            val editedThread =
                threadRepository.save(it.copy(content = command.content.trim(), modifiedAt = Instant.now()))
            return ThreadContentEdited(it.id, editedThread)
        }

    @Transactional
    override fun deleteThread(command: DeleteThread): ThreadDeleted =
        threadRepository.findById(command.threadId).orElseThrow(::DataNotFoundException).let {
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
