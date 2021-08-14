package waffle.guam.thread.event

import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import waffle.guam.image.ImageService
import waffle.guam.image.command.CreateImages
import waffle.guam.image.command.DeleteImages
import waffle.guam.image.model.ImageType

@Component
class ThreadEventHandler(
    private val imageService: ImageService,
) {

    @EventListener
    fun handleNoticeThreadSet(event: NoticeThreadSet) {}

    @EventListener
    fun handleThreadCreated(event: ThreadCreated) {
        if (!event.imageFiles.isNullOrEmpty())
            for (imageFile in event.imageFiles)
                imageService.createImages(
                    CreateImages(
                        files = event.imageFiles,
                        type = ImageType.THREAD,
                        parentId = event.threadId
                    )
                )
    }

    @EventListener
    fun handleThreadContentEdited(event: ThreadContentEdited) {}

    @EventListener
    fun handleThreadDeleted(event: ThreadDeleted) {
        imageService.deleteImages(DeleteImages.ByParentId(event.threadId, ImageType.THREAD))
    }
}
