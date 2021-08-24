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
import waffle.guam.task.command.ApplyTask
import waffle.guam.task.command.CancelTask
import waffle.guam.task.command.CompleteTask
import waffle.guam.task.command.CreateProjectTasks
import waffle.guam.task.command.IncOrDecProjectTasks

@Component
class ProjectEventHandler(
    private val projectStackService: ProjectStackService,
    private val imageService: ImageService,
    private val taskService: TaskService,
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

        taskService.handle(
            command = CreateProjectTasks(
                projectId = event.projectId,
                leaderId = event.leaderId,
                leaderPosition = event.leaderPosition,
                quotas = event.positionQuotas
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

        taskService.handle(
            command = IncOrDecProjectTasks(
                projectId = event.projectId,
                userId = event.userId,
                quotas = event.positionQuotas
            )
        )
    }

    @EventListener
    fun prjDeleted(event: ProjectDeleted) {
        logger.info("$event")

        taskService.handle(CancelTask(projectId = event.projectId))
    }

    @EventListener
    fun prjCompleted(event: ProjectCompleted) {
        logger.info("$event")

        taskService.handle(CompleteTask(projectId = event.projectId))
    }

    @EventListener
    fun prjJoinRequested(event: ProjectJoinRequested) {
        logger.info("$event")

        taskService.handle(
            ApplyTask(
                userId = event.userId,
                projectId = event.projectId,
                position = event.position,
            )
        )
    }
}