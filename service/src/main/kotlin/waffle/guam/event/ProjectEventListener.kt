package waffle.guam.event

import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import waffle.guam.service.MessageService

@Component
class ProjectEventListener(
    private val messageService: MessageService
) {

    @EventListener
    fun handleJoinRequestEvent(event: JoinRequestEvent) {
        messageService.sendMessage(
            ids = event.projectUserIds,
            title = event.projectTitle,
            body = "참여 신청이 들어왔습니다."
        )
    }

    @EventListener
    fun handleJoinResultEvent(event: JoinResultEvent) {
        messageService.sendMessage(
            ids = listOf(event.targetUserId),
            title = event.projectTitle,
            body = when (event.accepted) {
                true -> "참여 신청 승인되었습니다."
                false -> "참여 신청이 반려되었습니다."
            }
        )
    }
}
