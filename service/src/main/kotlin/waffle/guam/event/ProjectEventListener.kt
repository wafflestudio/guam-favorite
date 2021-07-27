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
            title = "프로젝트 참여 요청",
            body = "${event.projectTitle} 참여 요청이 도착했어요."
        )
    }

    @EventListener
    fun handleJoinResultEvent(event: JoinResultEvent) {
        messageService.sendMessage(
            ids = listOf(event.targetUserId),
            title = "프로젝트 참여 요청 결과",
            body = when (event.accepted) {
                true -> "${event.projectTitle}에 참가하게 되었어요."
                false -> "${event.projectTitle}에 참가하지 못하게 되었어요."
            }
        )
    }
}
