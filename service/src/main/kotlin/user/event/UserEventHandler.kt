package waffle.guam.user.event

import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import waffle.guam.image.ImageRepository
import waffle.guam.image.ImageService
import waffle.guam.image.command.CreateImages
import waffle.guam.image.command.DeleteImages
import waffle.guam.image.model.ImageType
import waffle.guam.user.UserRepository

@Component
class UserEventHandler(
    private val imageService: ImageService,
    private val userRepository: UserRepository,
    private val imageRepository: ImageRepository
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
                userRepository.findById(e.userId).get().image = null
                imageService.deleteImages(
                    command = DeleteImages.ByParentId(parentId = e.userId, imageType = ImageType.PROFILE)
                )
            }
            false -> {
                val event = imageService.createImages(
                    command = CreateImages(files = listOf(e.image), type = ImageType.PROFILE, parentId = e.userId)
                )
                userRepository.findById(e.userId).get().image =
                    imageRepository.findById(event.imageIds.first()).get()
            }
        }
    }

    @EventListener
    fun handle(e: DeviceUpdated) {
        logger.info("$e")
    }
}
