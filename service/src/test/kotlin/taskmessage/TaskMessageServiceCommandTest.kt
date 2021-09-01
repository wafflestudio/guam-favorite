package waffle.guam.taskmessage

import org.springframework.beans.factory.annotation.Autowired
import waffle.guam.annotation.DatabaseTest
import waffle.guam.task.TaskRepository
import javax.persistence.EntityManager

@DatabaseTest([])
class TaskMessageServiceCommandTest @Autowired constructor(
    private val entityManager: EntityManager,
    private val taskMessageRepository: TaskMessageRepository,
    private val taskRepository: TaskRepository
) {

    private val taskMessageService = TaskMessageServiceImpl(
        taskMessageRepository,
        taskRepository,
    )
}
