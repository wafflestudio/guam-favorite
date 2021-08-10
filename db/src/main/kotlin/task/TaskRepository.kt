package waffle.guam.task

import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository

interface TaskRepository : JpaRepository<TaskEntity, Long> {
    fun findAll(spec: Specification<TaskEntity>): List<TaskEntity>
}
