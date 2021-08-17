package waffle.guam.user.event

import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import waffle.guam.image.ImageService
import waffle.guam.image.command.CreateImages
import waffle.guam.image.command.DeleteImages
import waffle.guam.image.model.ImageType

@Component
class UserEventHandler(
    private val imageService: ImageService,
) {
    private val logger = LoggerFactory.getLogger(this::javaClass.name)

    @EventListener
    fun handle(e: UserCreated) {
        logger.info("$e")
    }

    @EventListener
    fun handle(e: UserUpdated) {
        if (!e.willUploadImage) {
            return
        }

        when (e.image == null) {
            true -> {
                imageService.deleteImages(
                    command = DeleteImages.ByParentId(parentId = e.userId, imageType = ImageType.PROFILE)
                )
            }
            false -> {
                imageService.createImages(
                    command = CreateImages(files = listOf(e.image), type = ImageType.PROFILE, parentId = e.userId)
                )
            }
        }
    }

    @EventListener
    fun handle(e: DeviceUpdated) {
        logger.info("$e")
    }
}
