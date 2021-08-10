package waffle.guam.task.command

import org.springframework.data.jpa.domain.Specification
import waffle.guam.task.TaskEntity
import waffle.guam.task.TaskSpec

data class TaskExtraFieldParams(
    val withTaskMsgs: Boolean = false,
) {
    val spec: Specification<TaskEntity> =
        TaskSpec.run {
            if (withTaskMsgs) fetchUser().and(fetchTaskMessages())
            else fetchUser()
        }
}
