package waffle.guam.controller

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import waffle.guam.common.UserContext
import waffle.guam.controller.request.SearchProject
import waffle.guam.controller.response.PageableResponse
import waffle.guam.controller.response.SuccessResponse
import waffle.guam.model.Project
import waffle.guam.service.ProjectService
import waffle.guam.service.command.CreateProject
import waffle.guam.service.command.JoinProject
import waffle.guam.service.command.UpdateProject

@RestController
@RequestMapping
class ProjectController(
    private val projectService: ProjectService
) {

    @PostMapping("/project")
    fun createProject(
        createProject: CreateProject,
        userContext: UserContext
    ): SuccessResponse<Project> =
        SuccessResponse(
            data = projectService.createProject(createProject, userContext.id)
        )

    @GetMapping("/project/list")
    fun getAllProjects(
        @RequestParam(required = true, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "20") size: Int,
    ): PageableResponse<Project> =
        projectService.getAllProjects(
            PageRequest.of(page, size, Sort.by("modifiedAt").descending())
        ).let {
            PageableResponse(
                data = it.content,
                size = it.content.size,
                offset = page * size,
                totalCount = it.totalElements.toInt(),
                hasNext = page * size + it.size < it.totalElements
            )
        }

    @GetMapping("/project/{id}")
    fun findProject(
        @PathVariable id: Long
    ): SuccessResponse<Project> =
        SuccessResponse(
            data = projectService.findProject(id)
        )

    @GetMapping("/project/tab")
    fun imminentProjects(
        @RequestParam(required = true, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "20") size: Int,
    ): PageableResponse<Project> =
        projectService.imminentProjects(
            PageRequest.of(page, size, Sort.by("modifiedAt").descending())
        ).let {
            PageableResponse(
                data = it.content,
                size = it.content.size,
                offset = page * size,
                totalCount = it.totalElements.toInt(),
                hasNext = page * size + it.size < it.totalElements
            )
        }

    @GetMapping("/project/search")
    fun searchProject(
        @RequestParam(required = true, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "20") size: Int,
        searchRequest: SearchProject
    ): PageableResponse<Project> =
        projectService.search(
            searchRequest.keyword, searchRequest.due,
            searchRequest.stackId, searchRequest.position,
            PageRequest.of(page, size, Sort.by("modifiedAt").descending())
        ).let {
            PageableResponse(
                data = it.content,
                size = it.content.size,
                offset = page * size,
                totalCount = it.totalElements.toInt(),
                hasNext = page * size + it.size < it.totalElements
            )
        }

    @PutMapping("/project/{id}")
    fun updateProject(
        @PathVariable id: Long,
        updateProject: UpdateProject,
        userContext: UserContext
    ): SuccessResponse<String> =
        SuccessResponse(
            data = projectService.updateProject(projectId = id, command = updateProject, userId = userContext.id)
        )

    @PostMapping("/project/{id}")
    fun joinProject(
        @PathVariable id: Long,
        @RequestBody jp: JoinProject,
        userContext: UserContext
    ): SuccessResponse<Boolean> =
        SuccessResponse(
            data = projectService.join(id, userContext.id, jp.position, jp.introduction)
        )

    @PostMapping("/project/{id}/{guestId}")
    fun acceptProjectJoinOrNot(
        @PathVariable id: Long,
        @PathVariable guestId: Long,
        @RequestParam accept: Boolean,
        userContext: UserContext
    ): SuccessResponse<String> =
        SuccessResponse(
            data = projectService.acceptOrNot(id, guestId, userContext.id, accept)
        )

    @PostMapping("/project/{id}/quit")
    fun quitProject(
        @PathVariable id: Long,
        userContext: UserContext
    ): SuccessResponse<String> =
        SuccessResponse(
            data = projectService.quit(id, userContext.id)
        )

    @DeleteMapping("/project/{id}")
    fun deleteProject(
        @PathVariable id: Long,
        userContext: UserContext
    ): SuccessResponse<String> =
        SuccessResponse(
            data = projectService.deleteProject(id, userContext.id)
        )
}
