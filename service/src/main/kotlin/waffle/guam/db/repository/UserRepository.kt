package waffle.guam.db.repository

import org.springframework.data.jpa.repository.JpaRepository
import waffle.guam.db.entity.UserEntity

interface UserRepository : JpaRepository<UserEntity, Long> {
    fun findByFirebaseUid(firebaseUid: String): UserEntity?
}
