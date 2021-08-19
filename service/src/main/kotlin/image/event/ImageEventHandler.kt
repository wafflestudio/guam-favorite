package waffle.guam.image.event

import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class ImageEventHandler {
    private val logger = LoggerFactory.getLogger(this::javaClass.name)

    @EventListener
    fun handle(event: ImagesCreated) {
        logger.info("$event")
    }

    @EventListener
    fun handle(event: ImagesDeleted) {
        logger.info("$event")
    }
}
