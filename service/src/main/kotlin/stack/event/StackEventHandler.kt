package waffle.guam.stack.event

import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import waffle.guam.image.ImageService
import waffle.guam.image.command.CreateImages
import waffle.guam.image.command.DeleteImages
import waffle.guam.image.model.ImageType

@Component
class StackEventHandler(
    private val imageService: ImageService
) {

    @EventListener
    fun stackCreated(event: StackCreated) {
        event.imageFiles?.let {
            imageService.createImages(
                command = CreateImages(
                    files = listOf(it),
                    type = ImageType.STACK,
                    parentId = event.stackId
                )
            )
        }
        TODO("return some response")
    }

    @EventListener
    fun stackUpdated(event: StackUpdated) {
        event.imageFiles?.let {
            imageService.deleteImages(
                command = DeleteImages.ByParentId(event.stackId, ImageType.STACK)
            )
            imageService.createImages(
                command = CreateImages(
                    files = listOf(it),
                    type = ImageType.STACK,
                    parentId = event.stackId
                )
            )
        }
        TODO("현재는 stack img 따로 삭제 불가")
    }

    @EventListener
    fun stackDeleted(event: StackDeleted) {
        TODO("return some response")
    }
}
