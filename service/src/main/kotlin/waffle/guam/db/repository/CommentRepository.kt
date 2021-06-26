package waffle.guam.db.repository

import org.springframework.data.jpa.repository.JpaRepository
import waffle.guam.db.entity.CommentEntity
import org.springframework.transaction.annotation.Transactional

interface CommentRepository : JpaRepository<CommentEntity, Long> {
    fun findByThreadId(threadId: Long): List<CommentEntity>
    fun countByThreadId(threadId: Long): Long

    @Transactional
    fun removeByThreadId(threadId: Long): Long
}
