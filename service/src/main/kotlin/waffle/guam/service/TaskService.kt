package waffle.guam.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import waffle.guam.db.entity.TaskStatus
import waffle.guam.db.repository.TaskMessageRepository
import waffle.guam.db.repository.TaskRepository
import waffle.guam.db.repository.TaskViewRepository
import waffle.guam.exception.DataNotFoundException
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
        taskRepository.findByUserId(userId).map { it.projectId }

    @Transactional
    fun getTaskWithMessages(taskId: Long): TaskDetail =
        taskViewRepository.findById(taskId).orElseThrow(::DataNotFoundException).let {
            TaskDetail.of(
                it, true
            )
        }

    @Transactional
    fun createTaskMsg(command: CreateTaskMsg) =
        taskMessageRepository.save(
            command.toEntity()
        )

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
    fun deleteTaskMsg(msgId: Long) =
        taskMessageRepository.findById(msgId).orElseThrow(::DataNotFoundException).let {
            it.status = TaskStatus.DELETED
            true
        }
}
