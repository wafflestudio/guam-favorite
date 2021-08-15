package waffle.guam.task.command

import org.springframework.data.jpa.domain.Specification
import waffle.guam.task.TaskEntity
import waffle.guam.task.TaskSpec

data class TaskExtraFieldParams(
    val withProject: Boolean = false,
    val withTaskMsgs: Boolean = false,
) {
    val spec: Specification<TaskEntity> =
        TaskSpec.run {
            when (withProject) {
                true -> fetchUser().and(fetchProject())
                else -> fetchUser()
            }.let {
                when (withTaskMsgs) {
                    true -> it.and(fetchTaskMessages())
                    false -> it
                }
            }
        }
}
