package waffle.guam.thread.event

import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class ThreadEventHandler {

    @EventListener
    fun handleNoticeThreadSet(event: NoticeThreadSet) {}

    @EventListener
    fun handleNoticeThreadRemoved(event: NoticeThreadRemoved) {}

    @EventListener
    fun handleThreadCreated(event: ThreadCreated) {}

    @EventListener
    fun handleThreadContentEdited(event: ThreadContentEdited) {}

    @EventListener
    fun handleThreadDeleted(event: ThreadDeleted) {}
}
