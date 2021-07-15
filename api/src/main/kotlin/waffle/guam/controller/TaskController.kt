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
import waffle.guam.controller.response.PageableResponse
import waffle.guam.controller.response.SuccessResponse
import waffle.guam.db.entity.TaskMessage
import waffle.guam.service.TaskService
import waffle.guam.service.command.CreateTaskMsg

@RestController
@RequestMapping
class TaskController(
    private val taskService: TaskService
) {
    // C
    @GetMapping("/task/{id}")
    fun getAllTaskMsg(
        @PathVariable id: Long,
        @RequestParam(required = true, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "20") size: Int,
    ): PageableResponse<TaskMessage> {
        return taskService.getAllMsg(PageRequest.of(page, size), id).let {
            PageableResponse(
                data = it.content,
                size = it.content.size,
                offset = page * size,
                totalCount = it.totalElements.toInt(),
                hasNext = page * size + it.size < it.totalElements
            )
        }
    }

    @PostMapping("/task/{id}")
    fun createTaskMsg(
        @PathVariable id: Long,
        @RequestBody createTaskMsg: CreateTaskMsg
    ): SuccessResponse<TaskMessage> =
        SuccessResponse(
            data = taskService.create(id, createTaskMsg)
        )

    @PutMapping("/taskMsg/{msgId}")
    fun updateTaskMsg(
        @PathVariable msgId: Long,
        @RequestBody createMsg: CreateTaskMsg
    ): SuccessResponse<TaskMessage> =
        SuccessResponse(
            data = taskService.update(msgId, createMsg)
        )

    // R
    @DeleteMapping("/taskMsg/{id}")
    fun deleteTaskMsg(
        @PathVariable msgId: Long
    ): SuccessResponse<Boolean> =
        SuccessResponse(
            data = taskService.delete(msgId)
        )
}
