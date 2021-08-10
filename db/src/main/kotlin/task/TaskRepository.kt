package waffle.guam.task

import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface TaskRepository : JpaRepository<TaskEntity, Long> {
    fun findAll(spec: Specification<TaskEntity>): List<TaskEntity>

    @Modifying
    @Query("UPDATE TaskEntity t SET t.userState = :userState WHERE t.id in :ids")
    fun updateStates(ids: List<Long>, userState: String): Int
}
