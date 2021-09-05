package waffle.guam.task

import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface TaskRepository : JpaRepository<TaskEntity, Long> {
    fun findAll(spec: Specification<TaskEntity>): List<TaskEntity>

    fun findAllByProjectId(projectId: Long): List<TaskEntity>

    fun findAllByProjectIdAndPosition(projectId: Long, position: String): List<TaskEntity>

    fun findAllByUserId(userId: Long): List<TaskEntity>

    @Query("select p.id from TaskEntity t left join t.project p inner join t.user u where u.id = :userId and p.state in :projectStates")
    fun findUserProjectIds(userId: Long, projectStates: List<String>): List<Long>

    fun findByProjectIdAndUserId(projectId: Long, userId: Long): TaskEntity?

    fun findByProjectIdAndUserState(projectId: Long, userState: String): TaskEntity?
}
