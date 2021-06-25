package waffle.guam.db.repository

import org.springframework.data.jpa.repository.JpaRepository
import waffle.guam.db.entity.TaskEntity

interface TaskRepository : JpaRepository<TaskEntity, Long> {
    fun findByUserId(userId: Long): List<TaskEntity>
}
