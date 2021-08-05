package waffle.guam.controller

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import waffle.guam.common.UserContext
import waffle.guam.controller.request.CreateTaskMsgInput
import waffle.guam.controller.request.UpdateTaskMsgInput
import waffle.guam.controller.response.SuccessResponse
import waffle.guam.db.entity.TaskMessage
import waffle.guam.model.TaskDetail
import waffle.guam.service.TaskService
import waffle.guam.service.command.CreateTaskMsg
import waffle.guam.service.command.UpdateTaskMsg

@RestController
@RequestMapping
class TaskController(
    private val taskService: TaskService
) {
    @GetMapping("/task/{taskId}")
    fun getTaskWithMessages(
        @PathVariable taskId: Long
    ): SuccessResponse<TaskDetail> =
        SuccessResponse(
            data = taskService.getTaskWithMessages(taskId)
        )

    @PostMapping("/task/{taskId}")
    fun createTaskMsg(
        @PathVariable taskId: Long,
        @RequestBody createTaskMsgInput: CreateTaskMsgInput,
        userContext: UserContext
    ): SuccessResponse<TaskMessage> =
        SuccessResponse(
            data = taskService.createTaskMsg(
                taskId = taskId,
                userId = userContext.id,
                command = CreateTaskMsg(
                    taskId = taskId,
                    msg = createTaskMsgInput.msg,
                    status = createTaskMsgInput.status,
                )
            )
        )

    @PutMapping("/taskMsg/{msgId}")
    fun updateTaskMsg(
        @PathVariable msgId: Long,
        @RequestBody updateTaskMsgInput: UpdateTaskMsgInput
    ): SuccessResponse<TaskMessage> =
        SuccessResponse(
            data = taskService.updateTaskMsg(
                command = UpdateTaskMsg(
                    msgId = msgId,
                    msg = updateTaskMsgInput.msg,
                    status = updateTaskMsgInput.status,
                )
            )
        )

    @DeleteMapping("/taskMsg/{msgId}")
    fun deleteTaskMsg(
        @PathVariable msgId: Long,
        userContext: UserContext
    ): SuccessResponse<Boolean> =
        SuccessResponse(
            data = taskService.deleteTaskMsg(
                userId = userContext.id,
                msgId = msgId
            )
        )
}
