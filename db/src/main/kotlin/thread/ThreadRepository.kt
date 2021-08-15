package waffle.guam.thread

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface ThreadRepository : JpaRepository<ThreadEntity, Long> {

    fun findByUserIdAndProjectId(userId: Long, projectId: Long): Optional<ThreadEntity>
}

interface ThreadViewRepository : JpaRepository<ThreadView, Long> {
    fun findByProjectId(projectId: Long, pageable: Pageable): Page<ThreadView>
}
