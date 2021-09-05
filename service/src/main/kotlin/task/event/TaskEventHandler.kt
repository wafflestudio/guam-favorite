package waffle.guam.task.event

import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import waffle.guam.thread.ThreadService
import waffle.guam.thread.ThreadViewRepository
import waffle.guam.thread.command.CreateJoinThread
import waffle.guam.thread.command.EditJoinThreadType
import waffle.guam.thread.model.ThreadType

@Component
class TaskEventHandler(
    private val threadService: ThreadService,
    private val threadViewRepository: ThreadViewRepository
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
