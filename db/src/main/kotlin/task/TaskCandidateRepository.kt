package waffle.guam.task

import org.springframework.data.jpa.repository.JpaRepository

interface TaskCandidateRepository : JpaRepository<TaskCandidateEntity, Long> {
    fun findByProjectIdAndUserId(projectId: Long, userId: Long): TaskCandidateEntity?

    fun deleteByProjectIdAndUserId(projectId: Long, userId: Long)
}
