package waffle.guam.image.event

import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import waffle.guam.image.ImageService

@Component
class ImageEventHandler(
    private val imageService: ImageService,
) {
    private val logger = LoggerFactory.getLogger(this::javaClass.name)

    @EventListener
    fun handle(event: ImagesDeleted) {
        logger.info("$event")
    }
}
