package waffle.guam.task.event

import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import waffle.guam.task.TaskRepository
import waffle.guam.thread.ThreadService
import waffle.guam.thread.command.CreateJoinThread
import waffle.guam.thread.command.EditJoinThreadType

@Component
class TaskEventHandler(
    private val taskRepository: TaskRepository,
    private val threadService: ThreadService
) {
    private val logger = LoggerFactory.getLogger(this.javaClass.name)

    @EventListener
    fun handle(event: TaskCreated) {
        logger.info("$event")
//        taskRepository.findAll().forEach {
//            println(it)
//        }
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
//        taskRepository.findAll().forEach {
//            println(it)
//        }
    }

    @EventListener
    fun handle(event: TaskLeft) {
        logger.info("$event")
        threadService.editJoinThreadType(
            command = EditJoinThreadType.toNormal(
                projectId = event.projectId,
                userId = event.userId
            )
        )
//        taskRepository.findAll().forEach {
//            println(it)
//        }
    }

    @EventListener
    fun handle(event: TaskAccepted) {
        logger.info("$event")
        threadService.editJoinThreadType(
            command = EditJoinThreadType.toNormal(
                projectId = event.projectId,
                userId = event.userId
            )
        )
//        taskRepository.findAll().forEach {
//            println(it)
//        }
    }

    @EventListener
    fun handle(event: TaskDeclined) {
        logger.info("$event")
        threadService.editJoinThreadType(
            command = EditJoinThreadType.toNormal(
                projectId = event.projectId,
                userId = event.userId
            )
        )
//        taskRepository.findAll().forEach {
//            println(it)
//        }
    }
}
