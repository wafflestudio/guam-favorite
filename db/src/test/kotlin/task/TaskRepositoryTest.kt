package waffle.guam.task

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import waffle.guam.annotation.DatabaseTest

@DatabaseTest(["images/image.sql", "task/user.sql", "task/project.sql", "task/task.sql", "task/task_message.sql"])
class TaskRepositoryTest @Autowired constructor(
    private val taskRepository: TaskRepository
) {
    @Transactional
    @Test
    fun fetchNothing() {
        taskRepository.findAll()
        /**
         *  (1)
         *  select * from tasks
         *  (2)
         *  select * from users X (task count)
         *  (3)
         *  select * from task_msgs X (task count)
         *  (4)
         *  select * from projects X (task count)
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
         *  (3)
         *  select * from projects X (task count)
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
         *  (2)
         *  select * from projects X (task count)
         */
    }

    @Transactional
    @Test
    fun fetchUserAndProject() {
        taskRepository.findAll(
            TaskSpec.fetchUser().and(TaskSpec.fetchProject())
        )

        /**
         * (1)
         * select * from tasks
         * inner join users
         * outer join images
         * inner join project
         * outer join images
         *
         * (2)
         * select * from task_msgs X (task count)
         */
    }

    @Transactional
    @Test
    fun fetchUserProjectIds() {
        val result = taskRepository.findUserProjectIds(1, listOf("ONGOING", "RECRUITING"))

        result.size shouldBe 3

        /**
         * 아이디 목록만 직접 가져오는 한 줄 쿼리
         */
    }

    @Transactional
    @Test
    fun fetchArchives() {
        val result = taskRepository.findAll(
            TaskSpec.run {
                fetchUser().and(
                    fetchArchive(listOf("RECRUITING", "ONGOING", "COMPLETED", "PENDING"))
                )
            }
        )

        result.size shouldBe 3

        /**
         *  fetch prj with state conditions
         *  한 줄
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
}
