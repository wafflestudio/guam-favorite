package waffle.guam.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import waffle.guam.db.entity.UserEntity
import waffle.guam.db.repository.UserRepository
import waffle.guam.exception.DataNotFoundException
import waffle.guam.model.User
import waffle.guam.service.command.UpdateUser
import java.time.Instant

@Service
class UserService(
    private val userRepository: UserRepository
) {

    fun get(id: Long): User =
        userRepository.findById(id).orElseThrow(::DataNotFoundException).let { User.of(it) }

    fun getByFirebaseUid(firebaseUid: String): User =
        User.of(
            userRepository.findByFirebaseUid(firebaseUid)
                ?: run { userRepository.save(UserEntity(firebaseUid = firebaseUid)) }
        )

    @Transactional
    fun update(command: UpdateUser): User =
        userRepository.findById(command.userId).orElseThrow(::DataNotFoundException).let {
            userRepository.save(
                it.copy(
                    name = command.name ?: it.name,
                    imageUrl = command.imageUrl ?: it.imageUrl,
                    skills = command.skills ?: it.skills,
                    githubUrl = command.githubUrl ?: it.githubUrl,
                    blogUrl = command.blogUrl ?: it.blogUrl,
                    introduction = command.introduction ?: it.introduction,
                    updatedAt = Instant.now()
                )
            ).let { updatedUser ->
                User.of(updatedUser)
            }
        }
}
