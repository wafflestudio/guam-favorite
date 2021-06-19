package waffle.guam.service

import org.springframework.stereotype.Service
import waffle.guam.db.entity.UserEntity
import waffle.guam.db.repository.UserRepository
import waffle.guam.model.User

@Service
class UserService(
    private val userRepository: UserRepository
) {

    fun getByFirebaseUid(firebaseUid: String): User =
        User.of(
            userRepository.findByFirebaseUid(firebaseUid)
                ?: run { userRepository.save(UserEntity(firebaseUid = firebaseUid)) }
        )
}
