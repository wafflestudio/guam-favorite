package waffle.guam.test

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import waffle.guam.Database
import waffle.guam.DatabaseTest
import waffle.guam.db.entity.UserEntity
import waffle.guam.db.repository.UserRepository
import waffle.guam.exception.DataNotFoundException
import waffle.guam.service.UserService
import waffle.guam.service.command.UpdateUser

@DatabaseTest
class UserServiceSpec (
    private val userRepository: UserRepository,
    private val database: Database
) : FeatureSpec() {

    private val userService = UserService(userRepository)

    init {
        beforeEach {
            database.cleanUp()
        }

        feature("회원 가입 기능") {
            scenario("등록되지 않은 파이어베이스 uid로 회원 정보 조회시, 자동으로 회원가입 한다.") {
                val result = userService.getByFirebaseUid("jon.snow.firebase")

                result shouldNotBe null
                result.isProfileSet shouldBe false
            }
        }

        feature("회원 조회 기능") {
            scenario("id에 해당하는 유저가 있다면 반환한다.") {
                val userId = userRepository.save(UserEntity(firebaseUid = "jon.snow.test")).id
                val result = userService.get(id = userId)

                result shouldNotBe null
                result.id shouldBe userId
            }

            scenario("id에 해당하는 유저가 없다면 예외가 발생한다.") {
                shouldThrowExactly<DataNotFoundException> {
                    userService.get(id = 404L)
                }
            }
        }

        feature("회원 수정 기능") {
            scenario("id에 해당하는 유저가 있다면 정보를 수정한다.") {
                val userId = userRepository.save(UserEntity(firebaseUid = "jon.snow.test")).id
                val result = userService.update(
                    command = UpdateUser(
                        nickname = "jony",
                        imageUrl = "s.s",
                        skills = "kotlin",
                        githubUrl = "s.s",
                        blogUrl = "s.s",
                        introduction = null
                    ),
                    userId = userId
                )

                result.id shouldBe userId
                result.nickname shouldBe "jony"
                result.introduction shouldBe null
            }

            scenario("id에 해당하는 유저가 없다면 예외가 발생한다.") {
                shouldThrowExactly<DataNotFoundException> {
                    userService.update(
                        command = UpdateUser(introduction = null),
                        userId = 404
                    )
                }
            }
        }
    }
}
