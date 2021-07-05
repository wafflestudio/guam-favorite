package waffle.guam.service

import org.springframework.stereotype.Service
import waffle.guam.db.repository.TaskRepository

@Service
class TaskService(
    private val taskRepository: TaskRepository
) {
    fun getProjectIds(userId: Long): List<Long> =
        taskRepository.findByUserId(userId).map { it.id }
}
