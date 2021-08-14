package waffle.guam.controller

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import waffle.guam.NotAllowedException
import waffle.guam.common.UserContext
import waffle.guam.controller.response.SuccessResponse
import waffle.guam.image.ImageService
import waffle.guam.image.command.DeleteImages
import waffle.guam.image.event.ImagesDeleted
import waffle.guam.thread.ThreadService

@RestController
@RequestMapping
class ThreadController(
    private val threadService: ThreadService,
    private val imageService: ImageService
) {

    @DeleteMapping("/thread/{threadId}/image/{imageId}")
    fun deleteThreadImage(
        @PathVariable threadId: Long,
        @PathVariable imageId: Long,
        userContext: UserContext
    ): SuccessResponse<ImagesDeleted> {
        threadService.getThread(threadId).let {
            if (it.creatorId != userContext.id) throw NotAllowedException()
            return SuccessResponse(
                imageService.deleteImages(DeleteImages.ById(listOf(imageId)))
            )
        }
        // TODO(클라이언트 추가 작업: content=null, image 개수가 1개인 쓰레드의 경우 deleteThread를 호출할 것)
        //    if (it.content.isBlank()) {
        //        if (imageRepository.findByParentIdAndType(it.id, ImageType.THREAD).size < 2)
        //            this.deleteThread(DeleteThread(threadId = it.id, userId = command.userId))
        //    }
    }
}
