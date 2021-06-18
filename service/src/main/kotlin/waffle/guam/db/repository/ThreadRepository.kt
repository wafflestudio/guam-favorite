package waffle.guam.db.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import waffle.guam.db.entity.ThreadEntity
import waffle.guam.db.entity.ThreadView

interface ThreadRepository : JpaRepository<ThreadEntity, Long>

interface ThreadViewRepository : JpaRepository<ThreadView, Long> {
    fun findByProjectId(projectId: Long, pageable: Pageable): Page<ThreadView>
}
