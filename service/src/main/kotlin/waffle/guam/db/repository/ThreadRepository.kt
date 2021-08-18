package waffle.guam.db.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import waffle.guam.db.entity.ThreadEntity
import waffle.guam.db.entity.ThreadView
import java.util.Optional

interface ThreadRepository : JpaRepository<ThreadEntity, Long> {

    fun findByUserIdAndProjectId(userId: Long, projectId: Long): Optional<ThreadEntity>

    fun countByUserIdAndProjectId(userId: Long, projectId: Long): Int
}

interface ThreadViewRepository : JpaRepository<ThreadView, Long> {
    fun findByProjectId(projectId: Long, pageable: Pageable): Page<ThreadView>

    @Modifying
    @Query("delete from ThreadView t where t.projectId = :projectId and t.user.id = :userId")
    fun deleteByProjectIdAndUserId(projectId: Long, userId: Long)
}
