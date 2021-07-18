package waffle.guam.controller

import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import waffle.guam.controller.request.CreateTaskMsgInput
import waffle.guam.controller.request.UpdateTaskMsgInput
import waffle.guam.controller.response.PageableResponse
import waffle.guam.controller.response.SuccessResponse
import waffle.guam.db.entity.TaskMessage
import waffle.guam.service.TaskService
import waffle.guam.service.command.CreateTaskMsg
import waffle.guam.service.command.UpdateTaskMsg

@RestController
@RequestMapping
class TaskController(
    private val taskService: TaskService
) {
    @GetMapping("/task/{taskId}")
    fun getAllTaskMsg(
        @PathVariable taskId: Long,
        @RequestParam(required = true, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "20") size: Int,
    ): PageableResponse<TaskMessage> {
        return taskService.getAllTaskMsg(PageRequest.of(page, size), taskId).let {
            PageableResponse(
                data = it.content,
                size = it.content.size,
                offset = page * size,
                totalCount = it.totalElements.toInt(),
                hasNext = page * size + it.size < it.totalElements
            )
        }
    }

    @PostMapping("/task/{taskId}")
    fun createTaskMsg(
        @PathVariable taskId: Long,
        @RequestBody createTaskMsgInput: CreateTaskMsgInput
    ): SuccessResponse<TaskMessage> =
        SuccessResponse(
            data = taskService.createTaskMsg(
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
        @PathVariable msgId: Long
    ): SuccessResponse<Boolean> =
        SuccessResponse(
            data = taskService.deleteTaskMsg(msgId)
        )
}
