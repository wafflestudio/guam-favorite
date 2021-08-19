package waffle.guam.comment

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.Optional

interface CommentRepository : JpaRepository<CommentEntity, Long> {

    @Query(
        "select c " +
            "from CommentEntity c left join fetch c.images i " +
            "where c.id = :commentId"
    )
    override fun findById(commentId: Long): Optional<CommentEntity>

    fun countByThreadId(threadId: Long): Long

    fun existsByThreadId(threadId: Long): Boolean
}
