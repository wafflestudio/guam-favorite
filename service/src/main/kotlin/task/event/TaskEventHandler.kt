package waffle.guam.task.event

import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import waffle.guam.message.MessageService
import waffle.guam.project.ProjectRepository
import waffle.guam.task.TaskRepository
import waffle.guam.task.model.UserState
import waffle.guam.thread.ThreadService
import waffle.guam.thread.ThreadViewRepository
import waffle.guam.thread.command.CreateJoinThread
import waffle.guam.thread.command.EditJoinThreadType
import waffle.guam.thread.model.ThreadType

@Component
class TaskEventHandler(
    private val threadService: ThreadService,
    private val threadViewRepository: ThreadViewRepository,
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository,
    private val messageService: MessageService,
) {
    private val logger = LoggerFactory.getLogger(this.javaClass.name)

    @EventListener
    fun handle(event: TaskCreated) {
        logger.info("$event")
    }

    @EventListener
    fun handle(event: TaskApplied) {
        logger.info("$event")

        threadService.createJoinThread(
            command = CreateJoinThread(
                projectId = event.projectId,
                userId = event.userId,
                content = event.introduction
            )
        )

        taskRepository.findByProjectIdAndUserState(
            projectId = event.projectId,
            userState = UserState.LEADER.name
        )?.let {
            messageService.sendMessage(
                ids = listOf(it.user!!.id),
                title = it.project.title,
                body = "참여 신청이 들어왔습니다."
            )
        }
    }

    @EventListener
    fun handle(event: TaskLeft) {
        logger.info("$event")
    }

    @EventListener
    fun handle(event: TaskAccepted) {
        logger.info("$event")

        threadService.editJoinThreadType(
            command = EditJoinThreadType(
                projectId = event.projectId,
                userId = event.userId,
                type = ThreadType.ACCEPTED
            )
        )

        projectRepository.findById(event.projectId).ifPresent {
            messageService.sendMessage(
                ids = listOf(event.userId),
                title = it.title,
                body = "참여 신청 승인되었습니다."
            )
        }
    }

    @EventListener
    fun handle(event: TaskDeclined) {
        logger.info("$event")

        threadService.editJoinThreadType(
            command = EditJoinThreadType(
                projectId = event.projectId,
                userId = event.userId,
                type = ThreadType.DECLINED
            )
        )

        projectRepository.findById(event.projectId).ifPresent {
            messageService.sendMessage(
                ids = listOf(event.userId),
                title = it.title,
                body = "참여 신청이 반려되었습니다."
            )
        }
    }

    @EventListener
    fun handle(event: TaskApplyCanceled) {
        logger.info("$event")

        threadViewRepository.findByProjectIdAndUserIdAndType(
            projectId = event.projectId,
            userId = event.userId,
            type = ThreadType.JOIN.name
        ).ifPresent {
            threadViewRepository.delete(it)
        }
    }
}
