package waffle.guam.taskmessage

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import waffle.guam.DataNotFoundException
import waffle.guam.NotAllowedException
import waffle.guam.task.TaskRepository
import waffle.guam.taskmessage.command.CreateTaskMessage
import waffle.guam.taskmessage.command.DeleteTaskMessage
import waffle.guam.taskmessage.command.UpdateTaskMessage
import waffle.guam.taskmessage.event.TaskMessageCreated
import waffle.guam.taskmessage.event.TaskMessageDeleted
import waffle.guam.taskmessage.event.TaskMessageUpdated
import waffle.guam.taskmessage.model.TaskStatus

@Service
class TaskMessageServiceImpl(
    private val taskMessageRepository: TaskMessageRepository,
    private val taskRepository: TaskRepository
) : TaskMessageService {

    @Transactional
    override fun createTaskMessage(command: CreateTaskMessage): TaskMessageCreated =
        taskRepository.findById(command.taskId).orElseThrow(::DataNotFoundException).let {
            if (it.user.id != command.userId) {
                throw NotAllowedException("이곳에는 작업 현황을 생성할 수 없습니다.")
            }

            return TaskMessageCreated(
                command.taskId,
                taskMessageRepository.save(command.toEntity()).id
            )
        }

    @Transactional
    override fun updateTaskMessage(command: UpdateTaskMessage): TaskMessageUpdated =
        taskMessageRepository.findById(command.taskMessageId).orElseThrow(::DataNotFoundException).let {
            taskMessageRepository.save(
                it.copy(
                    msg = command.msg ?: it.msg,
                    status = command.status?.name ?: it.status
                )
            )
            return TaskMessageUpdated(command.taskMessageId)
        }

    @Transactional
    override fun deleteTaskMessage(command: DeleteTaskMessage): TaskMessageDeleted =
        taskMessageRepository.findById(command.taskMessageId).orElseThrow(::DataNotFoundException).let {
            taskRepository.findByIdOrNull(it.taskId)?.let { taskMessageCreator ->
                if (taskMessageCreator.id != command.userId) {
                    throw NotAllowedException("해당 작업 현황을 삭제할 권한이 없습니다.")
                }
            }

            taskMessageRepository.save(
                it.copy(status = TaskStatus.DELETED.name)
            )
            return TaskMessageDeleted(command.taskMessageId)
        }
}
