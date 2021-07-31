package waffle.guam.db.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import waffle.guam.db.entity.UserEntity
import java.util.Optional

interface UserRepository : JpaRepository<UserEntity, Long> {
    @Query("select u from UserEntity u left join fetch u.image join fetch u.tasks t join fetch t.project where u.id = :userId")
    override fun findById(userId: Long): Optional<UserEntity>

    fun findByFirebaseUid(firebaseUid: String): UserEntity?
}
