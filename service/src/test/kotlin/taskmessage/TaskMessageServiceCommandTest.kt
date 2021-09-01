package waffle.guam.taskmessage

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import waffle.guam.InvalidRequestException
import waffle.guam.NotAllowedException
import waffle.guam.annotation.DatabaseTest
import waffle.guam.task.TaskRepository
import waffle.guam.taskmessage.command.CreateTaskMessage
import waffle.guam.taskmessage.command.DeleteTaskMessage
import waffle.guam.taskmessage.command.UpdateTaskMessage
import waffle.guam.taskmessage.model.TaskStatus
import javax.persistence.EntityManager

@DatabaseTest(["taskmessage/image.sql", "taskmessage/user.sql", "taskmessage/project.sql", "taskmessage/task.sql", "taskmessage/task_messages.sql"])
class TaskMessageServiceCommandTest @Autowired constructor(
    private val entityManager: EntityManager,
    private val taskMessageRepository: TaskMessageRepository,
    private val taskRepository: TaskRepository
) {

    private val taskMessageService = TaskMessageServiceImpl(
        taskMessageRepository,
        taskRepository,
    )

    @DisplayName("작업현황 생성 : 리더와 멤버는 자신에 대해 새로운 작업현황을 생성할 수 있다.")
    @Transactional
    @Test
    fun createTaskMessageOK() {
        val command = CreateTaskMessage(
            userId = 1,
            taskId = 11,
            messageContent = "task 11의 새로운 작업현황",
            status = TaskStatus.ONGOING
        )

        val event = taskMessageService.createTaskMessage(command)

        entityManager.flush()

        val createdTaskMessage = taskMessageRepository.getById(4)

        event.taskId shouldBe 11
        event.taskMessageId shouldBe 4

        createdTaskMessage.taskId shouldBe 11
        createdTaskMessage.content shouldBe "task 11의 새로운 작업현황"
        createdTaskMessage.status shouldBe TaskStatus.ONGOING.name
    }

    @DisplayName("작업현황 생성 예외 : 타인에 대해서는 작업현황을 생성할 수 없다.")
    @Transactional
    @Test
    fun createTaskMessageTaskNotAllowedException() {
        shouldThrowExactly<NotAllowedException> {
            taskMessageService.createTaskMessage(
                command = CreateTaskMessage(
                    userId = 1,
                    taskId = 13,
                    messageContent = "리더도 멤버도 아닌 사용자가 작업현황 생성 시도",
                    status = TaskStatus.ONGOING
                )
            )
        }
    }

    @DisplayName("작업현황 수정 : 자신의 기존 작업현황의 내용과 상태를 수정할 수 있다.")
    @Transactional
    @Test
    fun updateTaskMessageOK() {
        val command = UpdateTaskMessage(
            userId = 1,
            taskMessageId = 1,
            messageContent = "task 10의 완료된 작업현황 - 수정본",
            status = TaskStatus.ONGOING
        )
        val prevTaskMessage = taskMessageRepository.getById(1).copy()

        val event = taskMessageService.updateTaskMessage(command)

        entityManager.flush()
        entityManager.clear()

        val updatedTaskMessage = taskMessageRepository.getById(1)

        event.taskMessageId shouldBe 1

        prevTaskMessage.taskId shouldBe 10
        prevTaskMessage.content shouldBe "task 10의 완료된 작업현황"
        prevTaskMessage.status shouldBe TaskStatus.DONE.name

        updatedTaskMessage.taskId shouldBe 10
        updatedTaskMessage.content shouldBe "task 10의 완료된 작업현황 - 수정본"
        updatedTaskMessage.status shouldBe TaskStatus.ONGOING.name
    }

    @DisplayName("작업현황 수정 예외 : 타인의 기존 작업현황의 내용과 상태를 수정할 수 없다.")
    @Transactional
    @Test
    fun updateTaskMessageNotAllowedException() {
        shouldThrowExactly<NotAllowedException> {
            taskMessageService.updateTaskMessage(
                command = UpdateTaskMessage(
                    userId = 9999,
                    taskMessageId = 1,
                    messageContent = "타인의 작업현황 수정 시도",
                    status = TaskStatus.DONE
                )
            )
        }
    }

    @DisplayName("작업현황 수정 예외 : 기존 작업현황의 내용과 동일하면 수정될 수 없다.")
    @Transactional
    @Test
    fun updateTaskMessageInvalidRequestException() {
        shouldThrowExactly<InvalidRequestException> {
            taskMessageService.updateTaskMessage(
                command = UpdateTaskMessage(
                    userId = 1,
                    taskMessageId = 1,
                    messageContent = "task 10의 완료된 작업현황",
                    status = TaskStatus.DONE
                )
            )
        }
    }

    @DisplayName("작업현황 삭제 : 본인에 대해 생성한 작업현황을 본인은 삭제할 수 있다.")
    @Transactional
    @Test
    fun deleteTaskMessageOK() {
        val command = DeleteTaskMessage(
            userId = 1,
            taskMessageId = 2,
        )
        val prevTaskMessage = taskMessageRepository.getById(2).copy()

        val event = taskMessageService.deleteTaskMessage(command)

        entityManager.flush()
        entityManager.clear()

        val deletedTaskMessage = taskMessageRepository.getById(2)

        event.taskMessageId shouldBe 2

        prevTaskMessage.taskId shouldBe 10
        prevTaskMessage.content shouldBe "task 10의 진행중인 작업현황"
        prevTaskMessage.status shouldBe TaskStatus.ONGOING.name

        deletedTaskMessage.taskId shouldBe 10
        deletedTaskMessage.content shouldBe "task 10의 진행중인 작업현황"
        deletedTaskMessage.status shouldBe TaskStatus.DELETED.name
    }

    @DisplayName("작업현황 삭제 예외 : 타인의 작업현황을 삭제할 수는 없다.")
    @Transactional
    @Test
    fun deleteTaskMessageNotAllowedException() {
        shouldThrowExactly<NotAllowedException> {
            taskMessageService.deleteTaskMessage(
                command = DeleteTaskMessage(
                    userId = 3,
                    taskMessageId = 1,
                )
            )
        }
    }
}
