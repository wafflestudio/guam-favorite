package waffle.guam.db.repository

import org.springframework.data.jpa.repository.JpaRepository
import waffle.guam.db.entity.ProjectStackEntity

interface ProjectStackRepository : JpaRepository<ProjectStackEntity, Long>
