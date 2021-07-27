package waffle.guam.db.repository

import org.springframework.data.jpa.repository.JpaRepository
import waffle.guam.db.entity.ProjectStackEntity
import waffle.guam.db.entity.ProjectStackView

interface ProjectStackRepository : JpaRepository<ProjectStackEntity, Long>

interface ProjectStackViewRepository : JpaRepository<ProjectStackView, Long>
