package waffle.guam.comment

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface CommentRepository : JpaRepository<CommentEntity, Long> {

    @Query(
        "SELECT c " +
            "FROM threads t JOIN FETCH comments c " +
            "ON c.thread_id = :threadId"
    )
    fun findByThreadId(threadId: Long): List<CommentEntity>

    fun countByThreadId(threadId: Long): Long
}
