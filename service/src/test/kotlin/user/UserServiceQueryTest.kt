package waffle.guam.user

import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.shouldBe
import waffle.guam.annotation.DatabaseTest
import waffle.guam.task.TaskRepository
import waffle.guam.user.command.UserExtraFieldParams

@DatabaseTest(["user/image.sql", "user/user.sql", "user/project.sql", "user/task.sql", "user/task_message.sql"])
class UserServiceQueryTest(
    userRepository: UserRepository,
    taskRepository: TaskRepository
) : FeatureSpec() {
    private val userService = UserServiceImpl(userRepository, taskRepository)

    init {
        feature("유저 조회") {
            scenario("기본 정보들을 조회한다.") {
                val result = userService.getUser(1L)

                result.id shouldBe 1L
                result.projects shouldBe null
            }

            scenario("프로젝트 정보를 함께 조회한다.") {
                val result = userService.getUser(1L, UserExtraFieldParams(withProjects = true))

                result.id shouldBe 1L
                result.projects!!.size shouldBe 3
            }
        }
    }
}
