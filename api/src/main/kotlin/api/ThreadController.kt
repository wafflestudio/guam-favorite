package waffle.guam.api

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
import waffle.guam.api.request.ContentInput
import waffle.guam.api.request.CreateFullInfoInput
import waffle.guam.api.response.PageableResponse
import waffle.guam.api.response.SuccessResponse
import waffle.guam.api.response.ThreadDetailResponse
import waffle.guam.api.response.ThreadOverViewResponse
import waffle.guam.common.UserContext
import waffle.guam.thread.ThreadService
import waffle.guam.thread.command.CreateThread
import waffle.guam.thread.command.DeleteThread
import waffle.guam.thread.command.DeleteThreadImage
import waffle.guam.thread.command.EditThreadContent
import waffle.guam.thread.command.SetNoticeThread

@RestController
@RequestMapping
class ThreadController(
    private val threadService: ThreadService,
) {

    @GetMapping("/project/{projectId}/threads")
    fun getThreads(
        @PathVariable projectId: Long,
        @PageableDefault(size = 10, page = 0, sort = ["id"], direction = Sort.Direction.DESC) pageable: Pageable
    ): PageableResponse<ThreadOverViewResponse> =
        threadService.getThreads(projectId, pageable).let {
            PageableResponse(
                data = it.content.map { ThreadOverViewResponse.of(it) }.asReversed(),
                size = it.content.size,
                offset = it.pageable.offset.toInt(),
                totalCount = it.totalElements.toInt(),
                hasNext = it.pageable.offset + it.size < it.totalElements
            )
        }

    @GetMapping("/thread/{threadId}")
    fun getFullThread(
        @PathVariable threadId: Long
    ): SuccessResponse<ThreadDetailResponse> =
        SuccessResponse(
            ThreadDetailResponse.of(threadService.getFullThread(threadId))
        )

    @PutMapping("/project/{projectId}/notice/{threadId}")
    fun setNoticeThread(
        @PathVariable projectId: Long,
        @PathVariable threadId: Long,
        userContext: UserContext
    ): SuccessResponse<Unit> =
        threadService.setNoticeThread(
            command = SetNoticeThread(projectId = projectId, threadId = threadId, userId = userContext.id)
        ).let { SuccessResponse(Unit) }

    @PutMapping("/project/{projectId}/notice/reset")
    fun removeNoticeThread(
        @PathVariable projectId: Long,
        userContext: UserContext
    ): SuccessResponse<Unit> =
        threadService.setNoticeThread(
            command = SetNoticeThread(projectId = projectId, threadId = null, userId = userContext.id)
        ).let { SuccessResponse(Unit) }

    @PostMapping("/thread/create/{projectId}")
    fun createThread(
        @PathVariable projectId: Long,
        @ModelAttribute("contentAndOrImages") createFullInfoInput: CreateFullInfoInput,
        userContext: UserContext
    ): SuccessResponse<Unit> =
        threadService.createThread(
            command = CreateThread(
                projectId = projectId,
                userId = userContext.id,
                content = createFullInfoInput.content,
                imageFiles = createFullInfoInput.imageFiles
            )
        ).let { SuccessResponse(Unit) }

    @PutMapping("/thread/{threadId}/content")
    fun editThreadContent(
        @PathVariable threadId: Long,
        @RequestBody contentInput: ContentInput,
        userContext: UserContext
    ): SuccessResponse<Unit> =
        threadService.editThreadContent(
            command = EditThreadContent(
                threadId = threadId,
                userId = userContext.id,
                content = contentInput.content
            )
        ).let { SuccessResponse(Unit) }

    @DeleteMapping("/thread/{threadId}/image/{imageId}")
    fun deleteThreadImage(
        @PathVariable threadId: Long,
        @PathVariable imageId: Long,
        userContext: UserContext
    ): SuccessResponse<Unit> =
        threadService.deleteThreadImage(
            command = DeleteThreadImage(
                threadId = threadId,
                imageId = imageId,
                userId = userContext.id
            )
        ).let { SuccessResponse(Unit) }

    @DeleteMapping("/thread/{threadId}")
    fun deleteThread(
        @PathVariable threadId: Long,
        userContext: UserContext
    ): SuccessResponse<Unit> =
        threadService.deleteThread(
            command = DeleteThread(threadId = threadId, userId = userContext.id)
        ).let { SuccessResponse(Unit) }
}
