package waffle.guam.projectstack.event

import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class ProjectStackEventHandler() {
    private val logger = LoggerFactory.getLogger(this::javaClass.name)

    @EventListener
    fun prjStacksCreated(event: ProjectStacksCreated) {
        logger.info("$event")
    }

    @EventListener
    fun prjStacksUpdated(event: ProjectStacksUpdated) {
        logger.info("$event")
    }
}
