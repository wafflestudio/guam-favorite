package waffle.guam.projectstack.event

import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class ProjectStackEventHandler() {

    @EventListener
    fun prjStacksCreated(event: ProjectStacksCreated) {
        TODO()
    }

    @EventListener
    fun prjStacksUpdated(event: ProjectStacksUpdated) {
        TODO()
    }
}
