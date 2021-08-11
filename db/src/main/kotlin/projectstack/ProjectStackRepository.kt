package waffle.guam.projectstack

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface ProjectStackRepository : JpaRepository<ProjectStackEntity, Long> {

    fun findByProjectIdAndPosition(projectId: Long, position: String): Optional<ProjectStackEntity>

    fun findByProjectId(projectId: Long): List<ProjectStackEntity>

    fun findAllByProjectIds(projectIds: List<Long>): List<ProjectStackEntity>
}
