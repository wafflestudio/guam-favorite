package waffle.guam.controller

import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.*
import waffle.guam.controller.response.PageableResponse
import waffle.guam.controller.response.SuccessResponse
import waffle.guam.db.entity.TaskMessage
import waffle.guam.model.TechStack
import waffle.guam.service.TaskService
import waffle.guam.service.command.CreateTaskMsg

@RestController
@RequestMapping
class TaskController(
    private val taskService: TaskService
) {
    // C
    @GetMapping("/task/{id}")
    @ResponseBody
    fun getAllTaskMsg(
        @PathVariable id: Long,
        @RequestParam(required = true, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "20") size: Int,
    ): PageableResponse<TaskMessage> {
        return taskService.getAllMsg(PageRequest.of(page, size), id).let{
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
    @ResponseBody
    fun createTaskMsg(
        @PathVariable id: Long,
        @RequestBody createTaskMsg: CreateTaskMsg
    ): SuccessResponse<TaskMessage> =
        SuccessResponse(
            data = taskService.create(id, createTaskMsg)
        )


    @PutMapping("/taskMsg/{msgId}")
    @ResponseBody
    fun updateTaskMsg(
        @PathVariable msgId: Long,
        @RequestBody createMsg: CreateTaskMsg
    ): SuccessResponse<TaskMessage> =
        SuccessResponse(
            data = taskService.update(msgId, createMsg)
        )

    // R
    @DeleteMapping("/taskMsg/{id}")
    @ResponseBody
    fun deleteTaskMsg(
        @PathVariable msgId: Long
    ): SuccessResponse<Boolean> =
        SuccessResponse(
            data = taskService.delete(msgId)
        )
}
