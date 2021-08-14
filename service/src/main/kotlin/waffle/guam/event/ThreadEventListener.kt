package waffle.guam.event

import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import waffle.guam.db.entity.UserState
import waffle.guam.db.repository.ProjectViewRepository
import waffle.guam.db.repository.UserRepository
import waffle.guam.service.MessageService

@Component
class ThreadEventListener(
    private val messageService: MessageService,
    private val projectViewRepository: ProjectViewRepository,
    private val userRepository: UserRepository
) {
    @EventListener
    fun handleThreadCreatedEvent(event: ThreadCreatedEvent) {
        val targetIds = projectViewRepository.findById(event.projectId).get().run {
            tasks.filter { it.userState == UserState.MEMBER || it.userState == UserState.LEADER }
                .map { it.user.id }
                .filter { event.creatorId != it }
        }
        val userName = userRepository.findById(event.creatorId).get().nickname

        messageService.sendMessage(
            ids = targetIds,
            title = "새로운 스레드가 작성되었습니다.",
            body = "$userName: ${event.content}"
        )
    }

    @EventListener
    fun handleCommentCreatedEvent(event: CommentCreatedEvent) {
        if (event.threadCreatorId == event.commentCreatorId) return

        val userName = userRepository.findById(event.commentCreatorId).get().nickname

        messageService.sendMessage(
            ids = listOf(event.threadCreatorId),
            title = "새로운 답글이 달렸습니다.",
            body = "$userName: ${event.content}"
        )
    }
}
