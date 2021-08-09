package waffle.guam.image

import waffle.guam.image.command.CreateImages
import waffle.guam.image.command.DeleteImages
import waffle.guam.image.event.ImagesCreated
import waffle.guam.image.event.ImagesDeleted

interface ImageService {
    fun createImages(command: CreateImages): ImagesCreated
    fun deleteImages(command: DeleteImages): ImagesDeleted
}
