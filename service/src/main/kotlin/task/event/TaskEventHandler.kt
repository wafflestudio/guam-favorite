package waffle.guam.task.event

import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import waffle.guam.task.TaskRepository

@Component
class TaskEventHandler(
    private val taskRepository: TaskRepository
) {
    private val logger = LoggerFactory.getLogger(this.javaClass.name)

    @EventListener
    fun handle(event: TaskCreated) {
        logger.info("$event")
        taskRepository.findAll().forEach {
            println(it)
        }
    }

    @EventListener
    fun handle(event: TaskJoined) {
        logger.info("$event")
        taskRepository.findAll().forEach {
            println(it)
        }
    }

    @EventListener
    fun handle(event: TaskLeft) {
        logger.info("$event")
        taskRepository.findAll().forEach {
            println(it)
        }
    }

    @EventListener
    fun handle(event: TaskAccepted) {
        logger.info("$event")
        taskRepository.findAll().forEach {
            println(it)
        }
    }

    @EventListener
    fun handle(event: TaskDeclined) {
        logger.info("$event")
        taskRepository.findAll().forEach {
            println(it)
        }
    }
}
