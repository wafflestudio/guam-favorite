package waffle.guam.db.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import waffle.guam.db.entity.Due
import waffle.guam.db.entity.ProjectEntity
import waffle.guam.db.entity.ProjectView

interface ProjectRepository : JpaRepository<ProjectEntity, Long>, JpaSpecificationExecutor<ProjectEntity>

interface ProjectViewRepository : JpaRepository<ProjectView, Long>, JpaSpecificationExecutor<ProjectView> {

    fun findByFrontHeadcountIsLessThanOrBackHeadcountIsLessThanOrDesignerHeadcountIsLessThan(
        frontHeadcount: Int = 2,
        backHeadcount: Int = 2,
        designerHeadcount: Int = 2
    ): List<ProjectView>

    fun findByDueEquals(
        due: Due
    ): List<ProjectView>
}
