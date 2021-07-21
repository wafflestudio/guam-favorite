package waffle.guam.db.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import waffle.guam.db.entity.Due
import waffle.guam.db.entity.ProjectEntity
import waffle.guam.db.entity.ProjectView

interface ProjectRepository : JpaRepository<ProjectEntity, Long>

interface ProjectViewRepository : JpaRepository<ProjectView, Long> {
    fun findByFrontHeadcountIsLessThanOrBackHeadcountIsLessThanOrDesignerHeadcountIsLessThan(
        frontHeadcount: Int = 2,
        backHeadcount: Int = 2,
        designerHeadcount: Int = 2
    ): List<ProjectView>

    fun findByDueEquals(
        due: Due
    ): List<ProjectView>

    @Query("select p from ProjectView p left join p.tasks t left join t.user u")
    fun listAllProjects(pageable: Pageable): Page<ProjectView>
}
