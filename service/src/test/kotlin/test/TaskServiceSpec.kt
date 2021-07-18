package waffle.guam.test

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import waffle.guam.Database
import waffle.guam.DatabaseTest
import waffle.guam.db.entity.Position
import waffle.guam.db.entity.State
import waffle.guam.db.entity.TaskEntity
import waffle.guam.db.entity.TaskStatus
import waffle.guam.db.repository.TaskMessageRepository
import waffle.guam.db.repository.TaskRepository
import waffle.guam.db.repository.TaskViewRepository
import waffle.guam.service.TaskService
import waffle.guam.service.command.CreateTaskMsg

@DatabaseTest
class TaskServiceSpec @Autowired constructor(
    private val taskRepository: TaskRepository,
    private val taskViewRepository: TaskViewRepository,
    private val taskMessageRepository: TaskMessageRepository,
    private val database: Database,
) {

    private val taskService = TaskService(
        taskRepository = taskRepository,
        taskViewRepository = taskViewRepository,
        taskMessageRepository = taskMessageRepository,
    )

    @BeforeEach
    fun clearDatabase() {
        database.cleanUp()
    }

    // getAllTaskMsg

    @DisplayName("태스크 메시지 생성 : 특정 task id에 대해 태스크 메시지 생성 가능")
    @Transactional
    @Test
    fun createTaskMsgOK() {
        val users = database.getUsers()
        database.getProject()
        taskRepository.save(DefaultInput.task)
        val secondTask = taskRepository.save(DefaultInput.task.copy(userId = users[1].id))
        val taskMsg = taskService.createTaskMsg(
            DefaultCommand.CreateTaskMsg.copy(
                taskId = secondTask.id,
                msg = "This is a Task Message",
                status = TaskStatus.ONGOING
            )
        )

        taskMsg.id shouldBe 1
        taskMsg.taskId shouldBe secondTask.id
        taskMsg.msg shouldBe "This is a Task Message"
        taskMsg.status shouldBe TaskStatus.ONGOING
    }

    // updateTaskMsg

    // deleteTaskMsg

    object DefaultCommand {
        val CreateTaskMsg = CreateTaskMsg(
            taskId = 1,
            msg = "Task Message",
            status = TaskStatus.ONGOING
        )
    }

    object DefaultInput {
        val task = TaskEntity(
            position = Position.FRONTEND,
            projectId = 1,
            userId = 1,
            state = State.MEMBER
        )
    }
}
