package waffle.guam.task

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.transaction.annotation.Transactional
import waffle.guam.annotation.DatabaseTest
import waffle.guam.user.UserEntity
import waffle.guam.user.UserRepository
import javax.persistence.EntityManager

@DatabaseTest(["task/image.sql", "task/user.sql", "task/task.sql", "task/task_message.sql"])
class TaskRepositoryTest @Autowired constructor(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository,
    private val entityManager: EntityManager
) {
    @Transactional
    @Test
    fun fetchNothing() {
        taskRepository.findAll().forEach {
            println(it)
        }
        /**
         *  (1)
         *  select * from tasks
         *  (2)
         *  select * from users X (task count)
         *  (3)
         *  select * from task_msgs X (task count)
         */
    }

    @Transactional
    @Test
    fun fetchUser() {
        taskRepository.findAll(
            TaskSpec.fetchUser()
        )
        /**
         *  (1)
         *  select distinct * from tasks
         *  inner join users
         *  left join images
         *  (2)
         *  select * from task_msgs X (task count)
         */
    }

    @Transactional
    @Test
    fun fetchUserAndMessages() {
        taskRepository.findAll(
            TaskSpec.fetchUser().and(TaskSpec.fetchTaskMessages())
        )
        /**
         *  (1)
         *  select distinct * from tasks
         *  inner join users
         *  outer join images
         *  outer join task_msgs
         */
    }

    @Transactional
    @Test
    fun findByCondition() {
        val result = taskRepository.findAll(TaskSpec.userStates(listOf("MEMBER")))

        result.size shouldBe 2
    }

    @Transactional
    @Test
    fun findByConditions() {
        val result = taskRepository.findAll(
            TaskSpec.userStates(listOf("MEMBER")).and(
                TaskSpec.positions(listOf("BACKEND"))
            )
        )

        result.size shouldBe 1
    }

    @Transactional
    @Test
    fun saveTask() {
        taskRepository.save(
            TaskEntity(
                id = 0,
                projectId = 0,
                position = "",
                taskMessages = setOf(),
                user = UserEntity(
                    id = 1L, firebaseUid = "", status = "ACTIVE"
                ),
                userState = ""
            )
        )

        taskRepository.findAll().forEach {
            println(it)
        }

        TestTransaction.flagForCommit()
        TestTransaction.end()
        TestTransaction.start()

        taskRepository.findAll().forEach {
            println(it)
        }
    }
}
