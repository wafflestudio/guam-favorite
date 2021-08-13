package waffle.guam.comment

import org.springframework.data.jpa.repository.JpaRepository

interface CommentRepository : JpaRepository<CommentEntity, Long> {

    fun countByThreadId(threadId: Long): Long
}
