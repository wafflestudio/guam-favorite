package waffle.guam.db.repository

import org.springframework.data.jpa.repository.JpaRepository
import waffle.guam.db.entity.Position
import waffle.guam.db.entity.ProjectStackEntity
import waffle.guam.db.entity.ProjectStackView
import java.util.*

interface ProjectStackRepository : JpaRepository<ProjectStackEntity, Long> {

    fun findByProjectIdAndPosition(projectId: Long, position: Position): Optional<ProjectStackEntity>
}

interface ProjectStackViewRepository : JpaRepository<ProjectStackView, Long>
