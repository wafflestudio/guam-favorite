package waffle.guam.thread.event

import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import waffle.guam.comment.CommentRepository
import waffle.guam.image.ImageRepository
import waffle.guam.image.ImageService
import waffle.guam.image.command.CreateImages
import waffle.guam.image.command.DeleteImages
import waffle.guam.image.model.ImageType
import waffle.guam.task.TaskService
import waffle.guam.task.query.SearchTask.Companion.taskQuery
import waffle.guam.thread.ThreadRepository

@Component
class ThreadEventHandler(
    private val imageService: ImageService,
    private val taskService: TaskService,
    private val threadRepository: ThreadRepository,
    private val commentRepository: CommentRepository,
    private val imageRepository: ImageRepository
    // private val messageService: MessageService,
) {
    private val logger = LoggerFactory.getLogger(javaClass.name)

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

        val targetIds = taskService.getTasks(taskQuery().projectIds(event.projectId))
            .map { it.user.id }
            .filter { event.creatorId != it }
        //    messageService.sendMessage(
        //        ids = targetIds,
        //        title = "새로운 쓰레드가 작성되었습니다.",
        //        body = "${event.creatorName}: ${event.content}"
        //    )
    }

    @EventListener
    fun handle(event: JoinThreadCreated) {
        logger.info("$event")
    }

    @EventListener
    fun handle(event: ThreadContentEdited) {
        logger.info("$event")

        if (event.editedContent.isNotBlank()) return

        val threadImages = imageRepository.findByParentIdAndType(event.threadId, ImageType.THREAD.name)

        if (threadImages.isNotEmpty()) return

        if (!commentRepository.existsByThreadId(event.threadId)) {
            threadRepository.deleteById(event.threadId)
        }
    }

    @EventListener
    fun handle(event: ThreadTypeEdited) {
        logger.info("$event")
    }

    @EventListener
    fun handle(event: JoinThreadTypeEdited) {
        logger.info("$event")
    }

    @EventListener
    fun handle(event: ThreadDeleted) {
        imageService.deleteImages(DeleteImages.ByParentId(event.threadId, ImageType.THREAD))

        logger.info("$event")
    }
}
