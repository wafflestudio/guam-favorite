package waffle.guam.db.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import waffle.guam.db.entity.CommentEntity
import java.time.LocalDateTime

interface CommentRepository : JpaRepository<CommentEntity, Long> {
    fun findByThreadId(threadId: Long): List<CommentEntity>
    fun countByThreadId(threadId: Long): Long

    @Modifying
    @Query("update comments c set c.content = :content, c.modifiedAt = :modifiedAt where c.id = :id")
    fun updateContent(@Param(value = "id") id: Long, @Param(value = "content") content: String?, @Param(value = "modifiedAt") modifiedAt: LocalDateTime)
}
