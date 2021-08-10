package waffle.guam.user

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import waffle.guam.annotation.DatabaseTest

@DatabaseTest(["user/image.sql", "user/user.sql"])
class UserRepositoryTest @Autowired constructor(
    private val userRepository: UserRepository
) {
    @Test
    fun fetchNothing() {
        userRepository.findAll().forEach {
            println(it)
        }
        /**
         *  (1)
         *  select * from users
         *  (2)
         *  select * from images X (user count)
         */
    }

    @Test
    fun fetchImage() {
        userRepository.findById(1L).get().also {
            println(it)
        }

        userRepository.findByFirebaseUid("firebase-1")?.let {
            println(it)
        }
        /**
         *  (1)
         *  select * from users
         *  left join image
         */
    }
}
