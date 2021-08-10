package waffle.guam.user

import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<UserEntity, Long> {
    fun findByFirebaseUid(firebaseUid: String): UserEntity?
}
