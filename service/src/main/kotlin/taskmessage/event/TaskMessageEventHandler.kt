package waffle.guam.taskmessage.event

import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class TaskMessageEventHandler {
    private val logger = LoggerFactory.getLogger(this::javaClass.name)

    @EventListener
    fun handle(event: TaskMessageCreated) {
        logger.info("$event")
    }

    @EventListener
    fun handle(event: TaskMessageUpdated) {
        logger.info("$event")
    }

    @EventListener
    fun handle(event: TaskMessageDeleted) {
        logger.info("$event")
    }
}
