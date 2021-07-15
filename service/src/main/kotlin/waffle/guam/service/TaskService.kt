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

@Service
class TaskService(
    private val taskViewRepository: TaskViewRepository,
    private val taskMessageRepository: TaskMessageRepository
) {

    @Transactional
    fun create(taskId: Long, createTaskMsg: CreateTaskMsg) =
        taskMessageRepository.save(
            createTaskMsg.toEntity(taskId)
        )

    @Transactional
    fun update(msgId: Long, createTaskMsg: CreateTaskMsg) =
        taskMessageRepository.findById(msgId).orElseThrow(::DataNotFoundException).let {
            taskMessageRepository.save(
                it.copy( msg = createTaskMsg.msg ?: it.msg ,
                         status = createTaskMsg.status ?: it.status )
            )
        }

    @Transactional
    fun getAllMsg(pageable: Pageable, taskId: Long): Page<TaskMessage> =
        taskMessageRepository.findAllByTaskId(taskId, pageable)

    @Transactional
    fun delete(msgId: Long) =
        taskMessageRepository.deleteById(msgId).let { true }

}
