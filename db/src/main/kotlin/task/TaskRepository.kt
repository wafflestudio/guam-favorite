package waffle.guam.task

import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository

interface TaskRepository : JpaRepository<TaskEntity, Long> {
    fun findAll(spec: Specification<TaskEntity>): List<TaskEntity>

    fun findAllByProjectId(projectId: Long): List<TaskEntity>

    fun findAllByProjectIdAndPosition(projectId: Long, position: String): List<TaskEntity>

    fun findAllByUserId(userId: Long): List<TaskEntity>

    fun findByProjectIdAndUserId(projectId: Long, userId: Long): TaskEntity?
}
