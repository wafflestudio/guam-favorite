package waffle.guam.db.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import waffle.guam.db.entity.ProjectEntity
import waffle.guam.db.entity.ProjectView

interface ProjectRepository : JpaRepository<ProjectEntity, Long>, JpaSpecificationExecutor<ProjectEntity>

interface ProjectViewRepository : JpaRepository<ProjectView, Long>, JpaSpecificationExecutor<ProjectView>
