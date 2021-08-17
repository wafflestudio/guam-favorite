package waffle.guam.user

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import waffle.guam.DataNotFoundException
import waffle.guam.task.TaskRepository
import waffle.guam.task.TaskSpec
import waffle.guam.user.command.UpdateUser
import waffle.guam.user.command.UserExtraInfo
import waffle.guam.user.event.DeviceUpdated
import waffle.guam.user.event.UserCreated
import waffle.guam.user.event.UserUpdated
import waffle.guam.user.model.User
import waffle.guam.user.model.UserProject
import waffle.guam.user.model.UserStatus
import java.time.Instant

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val taskRepository: TaskRepository,
) : UserService {
    override fun getUserId(firebaseUid: String): Long =
        userRepository.findByFirebaseUid(firebaseUid)?.id ?: createUser(firebaseUid).userId

    override fun getUser(userId: Long, extraInfo: UserExtraInfo): User =
        userRepository.findById(userId).orElseThrow(::DataNotFoundException)
            .let { User.of(it) }
            .run {
                when (extraInfo.projects) {
                    true -> copy(projects = getProjectInfos(id))
                    false -> this
                }
            }

    override fun createUser(firebaseUid: String): UserCreated =
        userRepository.save(
            UserEntity(firebaseUid = firebaseUid, status = UserStatus.ACTIVE.name)
        ).let {
            UserCreated(userId = it.id, firebaseUid = firebaseUid)
        }

    @Transactional
    override fun updateUser(userId: Long, command: UpdateUser): UserUpdated =
        userRepository.findById(userId).orElseThrow(::DataNotFoundException).run {
            nickname = command.nickname ?: nickname
            skills = command.skills?.joinToString(",") ?: skills
            githubUrl = command.githubUrl ?: githubUrl
            blogUrl = command.blogUrl ?: blogUrl
            introduction = command.introduction ?: introduction
            modifiedAt = Instant.now()

            UserUpdated(
                userId = id,
                willUploadImage = command.willUploadImage,
                image = command.image,
            )
        }

    @Transactional
    override fun updateDeviceToken(userId: Long, fcmToken: String): DeviceUpdated =
        userRepository.findById(userId).orElseThrow(::DataNotFoundException).run {
            this.fcmToken = fcmToken

            DeviceUpdated(userId = userId, fcmToken = fcmToken)
        }

    private fun getProjectInfos(userId: Long): List<UserProject> =
        taskRepository.findAll(
            TaskSpec.run { userIds(listOf(userId)).and(fetchUser()).and(fetchProject()) }
        ).map {
            UserProject.of(it)
        }
}