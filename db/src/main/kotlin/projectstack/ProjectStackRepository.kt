package waffle.guam.projectstack

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface ProjectStackRepository : JpaRepository<ProjectStackEntity, Long> {

    @Query("select distinct s from ProjectStackEntity s inner join fetch s.techStack t where s.projectId = :projectId")
    fun findByProjectId(projectId: Long): List<ProjectStackEntity>

    @Query("select distinct s from ProjectStackEntity s inner join fetch s.techStack t where s.projectId in :projectIds")
    fun findAllByProjectIds(projectIds: List<Long>): List<ProjectStackEntity>

    @Query("delete from ProjectStackEntity s where s.projectId = :projectId and s.position in :positions")
    @Modifying
    fun deleteAllByProjectIdAndPositionIn(projectId: Long, positions: List<String>): Int
}
