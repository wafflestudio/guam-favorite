package waffle.guam.db.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.transaction.annotation.Transactional
import waffle.guam.db.entity.CommentEntity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.OneToMany
import javax.persistence.OneToOne

interface CommentRepository : JpaRepository<CommentEntity, Long> {
    fun findByThreadId(threadId: Long): List<CommentEntity>
    fun countByThreadId(threadId: Long): Long
//    fun removeByThreadId(threadId: Long): Long
}
