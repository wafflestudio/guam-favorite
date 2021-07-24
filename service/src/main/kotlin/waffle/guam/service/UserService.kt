package waffle.guam.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import waffle.guam.db.entity.ImageType
import waffle.guam.db.entity.UserEntity
import waffle.guam.db.repository.UserRepository
import waffle.guam.exception.DataNotFoundException
import waffle.guam.model.User
import waffle.guam.service.command.UpdateDevice
import waffle.guam.service.command.UpdateUser
import java.time.Instant

@Service
class UserService(
    private val userRepository: UserRepository,
    private val imageService: ImageService
) {

    fun get(id: Long): User =
        userRepository.findById(id).orElseThrow(::DataNotFoundException).let { User.of(it) }

    fun getByFirebaseUid(firebaseUid: String): User =
        User.of(
            userRepository.findByFirebaseUid(firebaseUid)
                ?: run { userRepository.save(UserEntity(firebaseUid = firebaseUid)) }
        )

    @Transactional
    fun update(command: UpdateUser, image: MultipartFile?, userId: Long): User =
        userRepository.findById(userId).orElseThrow(::DataNotFoundException).let {
            it.nickname = command.nickname ?: it.nickname
            it.skills = command.skills?.joinToString(",") ?: it.skills
            it.githubUrl = command.githubUrl ?: it.githubUrl
            it.blogUrl = command.blogUrl ?: it.blogUrl
            it.introduction = command.introduction ?: it.introduction
            it.updatedAt = Instant.now()
            it.image = when (command.willUploadImage) {
                true -> { image?.let { imageService.upload(it, ImageInfo(userId, ImageType.PROFILE)) } }
                false -> it.image
            }
            User.of(it)
        }

    @Transactional
    fun deleteImage(userId: Long) =
        userRepository.findById(userId).orElseThrow(::DataNotFoundException).also { userEntity ->
            userEntity.image = null
        }.let {
            User.of(it)
        }

    @Transactional
    fun updateDeviceId(command: UpdateDevice, userId: Long) =
        userRepository.findById(userId).orElseThrow(::DataNotFoundException).also { userEntity ->
            userEntity.deviceId = command.deviceId
        }.let {
            User.of(it)
        }
}
