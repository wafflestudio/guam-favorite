package waffle.guam.taskmessage.event

import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class TaskMessageEventHandler {

    @EventListener
    fun handleTaskMessageCreated(event: TaskMessageCreated) {}

    @EventListener
    fun handleTaskMessageUpdated(event: TaskMessageUpdated) {}

    @EventListener
    fun handleTaskMessageDeleted(event: TaskMessageDeleted) {}
}
