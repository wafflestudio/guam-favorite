package waffle.guam.user

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import waffle.guam.annotation.DatabaseTest
import waffle.guam.task.TaskCandidateRepository
import waffle.guam.task.TaskRepository
import waffle.guam.user.command.UpdateUser

@DatabaseTest(["user/image.sql", "user/user.sql"])
class UserServiceCommandTest @Autowired constructor(
    private val userRepository: UserRepository,
    taskRepository: TaskRepository,
    taskCandidateRepository: TaskCandidateRepository,
) {
    private val userService = UserServiceImpl(userRepository, taskRepository, taskCandidateRepository)

    @Transactional
    @Test
    fun updateOnlyNotNullParams() {
        val user = userService.getUser(1L)

        val command = UpdateUser(nickname = "jon.snow", willUploadImage = false)

        val event = userService.updateUser(
            userId = 1L,
            command = command
        )

        event.userId shouldBe 1L
        event.willUploadImage shouldBe false
        event.image shouldBe null

        val updatedUser = userService.getUser(1L)

        updatedUser shouldBe user.copy(nickname = command.nickname!!, modifiedAt = updatedUser.modifiedAt)
    }

    @Transactional
    @Test
    fun updateDeviceToken() {
        val token = "jon.snow.token"
        val event = userService.updateDeviceToken(2L, token)

        event.userId shouldBe 2L
        event.fcmToken shouldBe token

        userRepository.findById(2L).get().device_token shouldBe token
    }
}
