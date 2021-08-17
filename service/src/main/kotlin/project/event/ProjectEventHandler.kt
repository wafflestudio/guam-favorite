package waffle.guam.project.event

import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import waffle.guam.image.ImageService
import waffle.guam.image.command.CreateImages
import waffle.guam.image.command.DeleteImages
import waffle.guam.image.model.ImageType
import waffle.guam.projectstack.ProjectStackService
import waffle.guam.task.TaskService
import waffle.guam.task.command.CreateTask
import waffle.guam.task.command.SearchTask.Companion.taskQuery
import waffle.guam.task.command.UpdateTaskUserState
import waffle.guam.task.model.UserState

@Component
class ProjectEventHandler(
    private val projectStackService: ProjectStackService,
    private val imageService: ImageService,
    private val taskService: TaskService
) {

    private val logger = LoggerFactory.getLogger(this::javaClass.name)

    @EventListener
    fun prjCreated(event: ProjectCreated) {
        logger.info("$event")

        projectStackService.createProjectStacks(
            projectId = event.projectId,
            command = event.stackIdList
        )
        event.imageFiles?.run {
            imageService.createImages(
                command = CreateImages(
                    files = listOf(this),
                    type = ImageType.PROJECT,
                    parentId = event.projectId
                )
            )
        }
        taskService.createTask(
            userId = event.leaderId,
            command = CreateTask(
                projectId = event.projectId,
                position = event.leaderPosition,
                userState = UserState.LEADER
            )
        )
    }

    @EventListener
    fun prjUpdated(event: ProjectUpdated) {
        logger.info("$event")

        projectStackService.updateProjectStacks(
            projectId = event.projectId,
            command = event.stackIdList
        )
        event.imageFiles?.run {
            imageService.deleteImages(
                command = DeleteImages.ByParentId(event.projectId, ImageType.PROJECT)
            )
            imageService.createImages(
                command = CreateImages(
                    files = listOf(this),
                    type = ImageType.PROJECT,
                    parentId = event.projectId
                )
            )
        }
    }

    @EventListener
    fun prjDeleted(event: ProjectDeleted) {
        logger.info("$event")

        taskService.getTasks(
            command = taskQuery().projectIds(event.projectId)
        ).let { taskList ->
            taskService.updateTaskState(
                command = UpdateTaskUserState(
                    taskIds = taskList.map { it.id },
                    userState = UserState.QUIT
                )
            )
        }
    }

    @EventListener
    fun prjCompleted(event: ProjectCompleted) {
        logger.info("$event")

        taskService.getTasks(
            command = taskQuery().projectIds(event.projectId).userStates(UserState.LEADER, UserState.MEMBER)
        ).let { taskList ->
            taskService.updateTaskState(
                command = UpdateTaskUserState(
                    taskIds = taskList.map { it.id },
                    userState = UserState.CONTRIBUTED
                )
            )
        }
    }

    @EventListener
    fun prjJoinRequested(event: ProjectJoinRequested) {
        logger.info("$event")
        TODO("여기서 task 만들 수 없음; task level 에서 추가적인 validation 필요")
    }
}
