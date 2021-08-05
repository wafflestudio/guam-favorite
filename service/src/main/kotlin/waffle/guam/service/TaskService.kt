package waffle.guam.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import waffle.guam.db.entity.TaskStatus
import waffle.guam.db.repository.TaskMessageRepository
import waffle.guam.db.repository.TaskRepository
import waffle.guam.db.repository.TaskViewRepository
import waffle.guam.exception.DataNotFoundException
import waffle.guam.exception.NotAllowedException
import waffle.guam.model.TaskDetail
import waffle.guam.service.command.CreateTaskMsg
import waffle.guam.service.command.UpdateTaskMsg

@Service
class TaskService(
    private val taskRepository: TaskRepository,
    private val taskViewRepository: TaskViewRepository,
    private val taskMessageRepository: TaskMessageRepository
) {
    @Transactional
    fun getProjectIds(userId: Long): List<Long> =
        taskRepository.findByUserId(userId).orElseThrow()
            .map { it.projectId }

    @Transactional
    fun getTaskWithMessages(taskId: Long): TaskDetail =
        taskViewRepository.findById(taskId).orElseThrow(::DataNotFoundException).let {
            TaskDetail.of(
                it, true
            )
        }

    @Transactional
    fun createTaskMsg(taskId: Long, userId: Long, command: CreateTaskMsg) =
        taskRepository.findById(taskId).orElseThrow(::DataNotFoundException).let {
            if (it.userId != userId) throw NotAllowedException("이곳에는 작업 현황을 생성할 수 없습니다.")
            taskMessageRepository.save(
                command.toEntity()
            )
        }

    @Transactional
    fun updateTaskMsg(command: UpdateTaskMsg) =
        taskMessageRepository.findById(command.msgId).orElseThrow(::DataNotFoundException).let {
            taskMessageRepository.save(
                it.copy(
                    msg = command.msg ?: it.msg,
                    status = command.status ?: it.status
                )
            )
        }

    @Transactional
    fun deleteTaskMsg(userId: Long, msgId: Long) =
        taskMessageRepository.findById(msgId).orElseThrow(::DataNotFoundException).let { msg ->
            taskRepository.findByIdOrNull(msg.taskId)?.let {
                if (it.userId != userId) throw NotAllowedException("해당 작업 현황을 삭제할 권한이 없습니다.")
            }
            msg.status = TaskStatus.DELETED
            true
        }
}
