package waffle.guam.thread.event

import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import waffle.guam.image.ImageService
import waffle.guam.image.command.CreateImages
import waffle.guam.image.command.DeleteImages
import waffle.guam.image.model.ImageType
import waffle.guam.task.TaskService
import waffle.guam.task.command.SearchTask
import waffle.guam.task.model.UserState

@Component
class ThreadEventHandler(
    private val imageService: ImageService,
    private val taskService: TaskService,
    // private val messageService: MessageService,
) {
    private val logger = LoggerFactory.getLogger(this::javaClass.name)

    @EventListener
    fun handle(event: NoticeThreadSet) {
        logger.info("$event")
    }

    @EventListener
    fun handle(event: ThreadCreated) {
        if (!event.imageFiles.isNullOrEmpty()) {
            imageService.createImages(
                CreateImages(
                    files = event.imageFiles,
                    type = ImageType.THREAD,
                    parentId = event.threadId
                )
            )
        }

        logger.info("$event")

        val targetIds = taskService.getTasks(SearchTask.taskQuery().projectIds(event.projectId))
            .filter { it.userState == UserState.MEMBER || it.userState == UserState.LEADER }
            .map { it.user.id }
            .filter { event.creatorId != it }
        //    messageService.sendMessage(
        //        ids = targetIds,
        //        title = "새로운 쓰레드가 작성되었습니다.",
        //        body = "${event.creatorName}: ${event.content}"
        //    )
    }

    @EventListener
    fun handle(event: JoinRequestThreadCreated) {
        logger.info("$event")
    }
    // TODO(클라와 컴케 필수: 달린 이미지가 없는 쓰레드의 content를 ""로 만들려는 경우, deleteThread 호출하도록 수정)
    @EventListener
    fun handle(event: ThreadContentEdited) {
        logger.info("$event")
    }

    @EventListener
    fun handle(event: ThreadDeleted) {
        imageService.deleteImages(DeleteImages.ByParentId(event.threadId, ImageType.THREAD))
        logger.info("$event")
    }
}
