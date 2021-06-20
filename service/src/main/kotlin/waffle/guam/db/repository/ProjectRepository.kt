package waffle.guam.db.repository

import org.springframework.data.jpa.repository.JpaRepository
import waffle.guam.db.entity.ProjectEntity
import waffle.guam.db.entity.ProjectView

interface ProjectRepository : JpaRepository<ProjectEntity, Long>

interface ProjectViewRepository : JpaRepository<ProjectView, Long> {
    fun findByFrontHeadcountIsLessThanOrBackHeadcountIsLessThanOrDesignerHeadcountIsLessThan(
        frontHeadcount: Int = 2,
        backHeadcount: Int = 2,
        designerHeadcount: Int = 2
    ): List<ProjectView>
}
