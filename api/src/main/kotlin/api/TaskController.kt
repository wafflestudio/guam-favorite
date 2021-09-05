package waffle.guam.api

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import waffle.guam.api.response.SuccessResponse
import waffle.guam.api.response.TaskResponse
import waffle.guam.task.TaskService

@RestController
@RequestMapping
class TaskController(
    private val taskService: TaskService,
) {
    @GetMapping("/task/{taskId}")
    fun getTaskWithMessages(
        @PathVariable taskId: Long,
    ): SuccessResponse<TaskResponse> =
        SuccessResponse(
            data = TaskResponse.of(taskService.getTask(taskId))
        )
}
