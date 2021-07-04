package waffle.guam.db.repository

import org.springframework.data.jpa.repository.JpaRepository
import waffle.guam.db.entity.ProjectStackEntity

interface ProjectStackRepository : JpaRepository<ProjectStackEntity, Long> {

    fun findByProjectId(
        id: Long
    ): List<ProjectStackEntity>

    fun deleteByProjectIdAndTechStackId(
        id: Long,
        techStackId: Long
    )
}
