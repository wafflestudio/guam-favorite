package waffle.guam.task.event

import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import waffle.guam.task.TaskRepository
import waffle.guam.thread.ThreadService
import waffle.guam.thread.command.CreateJoinRequestThread

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
        threadService.createJoinRequestThread(
            command = CreateJoinRequestThread(
                projectId = event.projectId,
                userId = event.userId,
                content = "TODO"
            )
        )
//        taskRepository.findAll().forEach {
//            println(it)
//        }
    }

    @EventListener
    fun handle(event: TaskLeft) {
        logger.info("$event")
//        taskRepository.findAll().forEach {
//            println(it)
//        }
    }

    @EventListener
    fun handle(event: TaskAccepted) {
        logger.info("$event")
//        taskRepository.findAll().forEach {
//            println(it)
//        }
    }

    @EventListener
    fun handle(event: TaskDeclined) {
        logger.info("$event")
//        taskRepository.findAll().forEach {
//            println(it)
//        }
    }
}
