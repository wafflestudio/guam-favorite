package waffle.guam.api

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import waffle.guam.api.request.CreateTaskMessageInput
import waffle.guam.api.request.UpdateTaskMessageInput
import waffle.guam.api.response.SuccessResponse
import waffle.guam.common.UserContext
import waffle.guam.taskmessage.TaskMessageService
import waffle.guam.taskmessage.command.CreateTaskMessage
import waffle.guam.taskmessage.command.DeleteTaskMessage
import waffle.guam.taskmessage.command.UpdateTaskMessage

@RestController
@RequestMapping
class TaskMessageController(
    private val taskMessageService: TaskMessageService
) {

    @PostMapping("/task/{taskId}")
    fun createTaskMsg(
        @PathVariable taskId: Long,
        @RequestBody createTaskMsgInput: CreateTaskMessageInput,
        userContext: UserContext
    ): SuccessResponse<Unit> =
        taskMessageService.createTaskMessage(
            command = CreateTaskMessage(
                userId = userContext.id,
                taskId = taskId,
                messageContent = createTaskMsgInput.msg,
                status = createTaskMsgInput.status,
            )
        ).let { SuccessResponse(Unit) }

    @PutMapping("/taskMsg/{taskMessageId}")
    fun updateTaskMsg(
        @PathVariable taskMessageId: Long,
        @RequestBody updateTaskMsgInput: UpdateTaskMessageInput
    ): SuccessResponse<Unit> =
        taskMessageService.updateTaskMessage(
            command = UpdateTaskMessage(
                taskMessageId = taskMessageId,
                messageContent = updateTaskMsgInput.msg,
                status = updateTaskMsgInput.status,
            )
        ).let { SuccessResponse(Unit) }

    @DeleteMapping("/taskMsg/{taskMessageId}")
    fun deleteTaskMsg(
        @PathVariable taskMessageId: Long,
        userContext: UserContext
    ): SuccessResponse<Unit> =
        taskMessageService.deleteTaskMessage(
            command = DeleteTaskMessage(
                userId = userContext.id,
                taskMessageId = taskMessageId
            )
        ).let { SuccessResponse(Unit) }
}
