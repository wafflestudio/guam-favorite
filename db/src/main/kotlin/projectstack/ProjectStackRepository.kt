package waffle.guam.projectstack

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.Optional

interface ProjectStackRepository : JpaRepository<ProjectStackEntity, Long> {

    fun findByProjectIdAndPosition(projectId: Long, position: String): Optional<ProjectStackEntity>
}

interface ProjectStackViewRepository : JpaRepository<ProjectStackView, Long> {

    @Query("select distinct s from ProjectStackView s left join fetch s.techStack t where s.projectId = :projectId")
    fun findByProjectId(projectId: Long): List<ProjectStackView>

    @Query("select distinct s from ProjectStackView s left join fetch s.techStack t where s.projectId in :projectIds")
    fun findAllByProjectIds(projectIds: List<Long>): List<ProjectStackView>
}
