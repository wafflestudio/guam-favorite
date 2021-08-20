package waffle.guam.task

import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface TaskRepository : JpaRepository<TaskEntity, Long> {
    fun findAll(spec: Specification<TaskEntity>): List<TaskEntity>

    fun findAllByProjectId(projectId: Long): List<TaskEntity>

    fun findByUserIdAndProjectId(userId: Long, projectId: Long): TaskEntity?

    @Modifying
    @Query("UPDATE TaskEntity t SET t.userState = :userState WHERE t.id in :ids")
    fun updateAllByIdIn(ids: List<Long>, userState: String): Int

    @Modifying
    @Query("DELETE FROM TaskEntity t WHERE t.id in :ids")
    fun deleteByIdIn(ids: List<Long>): Int
}
