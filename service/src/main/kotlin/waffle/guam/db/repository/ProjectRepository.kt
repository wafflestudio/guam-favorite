package waffle.guam.db.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import waffle.guam.db.entity.ProjectDetailView
import waffle.guam.db.entity.ProjectEntity
import waffle.guam.db.entity.ProjectState
import waffle.guam.db.entity.ProjectView

interface ProjectRepository : JpaRepository<ProjectEntity, Long>, JpaSpecificationExecutor<ProjectEntity> {
    fun findByStateIsNotIn(states: List<ProjectState>, pageable: Pageable): Page<ProjectEntity>
}

interface ProjectViewRepository : JpaRepository<ProjectView, Long>, JpaSpecificationExecutor<ProjectView>

interface ProjectDetailViewRepository : JpaRepository<ProjectDetailView, Long>
