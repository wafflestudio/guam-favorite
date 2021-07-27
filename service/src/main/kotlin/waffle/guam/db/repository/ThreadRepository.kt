package waffle.guam.db.repository

import java.util.Optional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import waffle.guam.db.entity.ThreadEntity
import waffle.guam.db.entity.ThreadView

interface ThreadRepository : JpaRepository<ThreadEntity, Long> {

    fun findByUserIdAndProjectId(userId: Long, projectId: Long): Optional<ThreadEntity>

    fun countByUserIdAndProjectId(userId: Long, projectId: Long): Int
}

interface ThreadViewRepository : JpaRepository<ThreadView, Long> {
    fun findByProjectId(projectId: Long, pageable: Pageable): Page<ThreadView>
}
