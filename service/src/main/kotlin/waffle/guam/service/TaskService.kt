package waffle.guam.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import waffle.guam.db.entity.TaskMessage
import waffle.guam.db.repository.TaskMessageRepository
import waffle.guam.db.repository.TaskRepository
import waffle.guam.db.repository.TaskViewRepository
import waffle.guam.exception.DataNotFoundException
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
    fun getAllTaskMsg(pageable: Pageable, taskId: Long): Page<TaskMessage> =
        taskMessageRepository.findAllByTaskId(taskId, pageable)

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
        taskMessageRepository.deleteById(msgId).let { true }
}
