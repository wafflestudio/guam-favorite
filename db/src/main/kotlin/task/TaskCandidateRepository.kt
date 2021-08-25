package waffle.guam.task

import org.springframework.data.jpa.repository.JpaRepository

interface TaskCandidateRepository : JpaRepository<TaskCandidateEntity, Long> {
    fun findAllByProjectId(projectId: Long): List<TaskCandidateEntity>

    fun findAllByUserId(userId: Long): List<TaskCandidateEntity>

    fun findByProjectIdAndUserId(projectId: Long, userId: Long): TaskCandidateEntity?

    fun deleteByProjectIdAndUserId(projectId: Long, userId: Long)
}
