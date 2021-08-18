package waffle.guam.thread.event

import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import waffle.guam.image.ImageService
import waffle.guam.image.command.CreateImages
import waffle.guam.image.command.DeleteImages
import waffle.guam.image.model.ImageType

@Component
class ThreadEventHandler(
    //    private val messageService: MessageService,
    //    private val projectViewRepository: ProjectViewRepository,
    //    private val userRepository: UserRepository,
    private val imageService: ImageService,
) {

    @EventListener
    fun handleNoticeThreadSet(event: NoticeThreadSet) {}

    @EventListener
    fun handleThreadCreated(event: ThreadCreated) {
        if (event.imageFiles.isNullOrEmpty()) return

        imageService.createImages(
            CreateImages(
                files = event.imageFiles,
                type = ImageType.THREAD,
                parentId = event.threadId
            )
        )

        //    val targetIds = projectViewRepository.findById(event.projectId).get().run {
        //        tasks.filter { it.userState == UserState.MEMBER || it.userState == UserState.LEADER }
        //            .map { it.user.id }
        //            .filter { event.creatorId != it }
        //    }
        //    val userName = userRepository.findById(event.creatorId).get().nickname
        //
        //    messageService.sendMessage(
        //        ids = targetIds,
        //        title = "새로운 쓰레드가 작성되었습니다.",
        //        body = "$userName: ${event.content}"
        //    )
    }

    @EventListener
    fun handleJoinRequestThreadCreated(event: JoinRequestThreadCreated) {}

    @EventListener
    fun handleThreadContentEdited(event: ThreadContentEdited) {}

    @EventListener
    fun handleThreadDeleted(event: ThreadDeleted) {
        imageService.deleteImages(DeleteImages.ByParentId(event.threadId, ImageType.THREAD))
    }
}
