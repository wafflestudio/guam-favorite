package waffle.guam.controller

import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import waffle.guam.common.UserContext
import waffle.guam.controller.request.ContentInput
import waffle.guam.controller.request.CreateFullInfoInput
import waffle.guam.controller.response.PageableResponse
import waffle.guam.controller.response.SuccessResponse
import waffle.guam.image.ImageService
import waffle.guam.image.command.DeleteImages
import waffle.guam.image.event.ImagesDeleted
import waffle.guam.thread.ThreadService
import waffle.guam.thread.command.CreateThread
import waffle.guam.thread.command.DeleteThread
import waffle.guam.thread.command.EditThreadContent
import waffle.guam.thread.command.SetNoticeThread
import waffle.guam.thread.event.NoticeThreadSet
import waffle.guam.thread.event.ThreadContentEdited
import waffle.guam.thread.event.ThreadCreated
import waffle.guam.thread.event.ThreadDeleted
import waffle.guam.thread.model.ThreadDetail
import waffle.guam.thread.model.ThreadOverView

@RestController
@RequestMapping
class ThreadController(
    private val threadService: ThreadService,
    private val imageService: ImageService
) {

    @GetMapping("/project/{projectId}/threads")
    fun getThreads(
        @PathVariable projectId: Long,
        @PageableDefault(size = 10, page = 0, sort = ["id"], direction = Sort.Direction.DESC) pageable: Pageable
    ): PageableResponse<ThreadOverView> =
        threadService.getThreads(projectId, pageable).let {
            PageableResponse(
                data = it.content.asReversed(),
                size = it.content.size,
                offset = it.pageable.offset.toInt(),
                totalCount = it.totalElements.toInt(),
                hasNext = it.pageable.offset + it.size < it.totalElements
            )
        }

    @GetMapping("/thread/{threadId}")
    fun getFullThread(
        @PathVariable threadId: Long
    ): SuccessResponse<ThreadDetail> =
        SuccessResponse(
            threadService.getFullThread(threadId)
        )

    @PutMapping("/project/{projectId}/notice/{threadId}")
    fun setNoticeThread(
        @PathVariable projectId: Long,
        @PathVariable threadId: Long,
        userContext: UserContext
    ): SuccessResponse<NoticeThreadSet> =
        SuccessResponse(
            threadService.setNoticeThread(
                command = SetNoticeThread(projectId = projectId, threadId = threadId, userId = userContext.id)
            )
        )

    @PutMapping("/project/{projectId}/notice/reset")
    fun removeNoticeThread(
        @PathVariable projectId: Long,
        userContext: UserContext
    ): SuccessResponse<NoticeThreadSet> =
        SuccessResponse(
            threadService.setNoticeThread(
                command = SetNoticeThread(projectId = projectId, threadId = null, userId = userContext.id)
            )
        )

    @PostMapping("/thread/create/{projectId}")
    fun createThread(
        @PathVariable projectId: Long,
        @ModelAttribute("contentAndOrImages") createFullInfoInput: CreateFullInfoInput,
        userContext: UserContext
    ): SuccessResponse<ThreadCreated> =
        SuccessResponse(
            threadService.createThread(
                command = CreateThread(
                    projectId = projectId,
                    userId = userContext.id,
                    content = createFullInfoInput.content,
                    imageFiles = createFullInfoInput.imageFiles
                )
            )
        )

    @PutMapping("/thread/{threadId}/content")
    fun editThreadContent(
        @PathVariable threadId: Long,
        @RequestBody contentInput: ContentInput,
        userContext: UserContext
    ): SuccessResponse<ThreadContentEdited> =
        SuccessResponse(
            threadService.editThreadContent(
                command = EditThreadContent(
                    threadId = threadId,
                    userId = userContext.id,
                    content = contentInput.content
                )
            )
        )

    @DeleteMapping("/thread/{threadId}/image/{imageId}")
    fun deleteThreadImage(
        @PathVariable threadId: Long,
        @PathVariable imageId: Long,
        userContext: UserContext
    ): SuccessResponse<ImagesDeleted> {
        threadService.validateThreadOwner(threadId = threadId, userId = userContext.id)
        return SuccessResponse(
            imageService.deleteImages(DeleteImages.ById(listOf(imageId)))
        )
    }
    // TODO(클라이언트 추가 작업: content=null, image 개수가 1개인 쓰레드의 경우 deleteThread를 호출할 것)
    //    if (it.content.isBlank()) {
    //        if (imageRepository.findByParentIdAndType(it.id, ImageType.THREAD).size < 2)
    //            this.deleteThread(DeleteThread(threadId = it.id, userId = command.userId))
    //    }

    @DeleteMapping("/thread/{threadId}")
    fun deleteThread(
        @PathVariable threadId: Long,
        userContext: UserContext
    ): SuccessResponse<ThreadDeleted> =
        SuccessResponse(
            threadService.deleteThread(
                command = DeleteThread(threadId = threadId, userId = userContext.id)
            )
        )
}
