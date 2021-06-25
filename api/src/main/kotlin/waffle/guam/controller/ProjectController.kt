package waffle.guam.controller

import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import waffle.guam.common.UserContext
import waffle.guam.controller.response.PageableResponse
import waffle.guam.controller.response.SuccessResponse
import waffle.guam.model.Project
import waffle.guam.service.ProjectService
import waffle.guam.service.command.CreateProject

@RestController
@RequestMapping
class ProjectController(
    private val projectService: ProjectService,
) {

    @PostMapping("/project")
    @ResponseBody
    fun createProject(
        @RequestBody createProject: CreateProject,
        userContext: UserContext
    ): SuccessResponse<Project> =
        SuccessResponse(
            data = projectService.createProject(createProject, userContext.id)
        )

    @GetMapping("/project/list")
    @ResponseBody
    fun getAllProjects(
        @RequestParam(required = true, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "20") size: Int,
    ): PageableResponse<Project> =
        projectService.getAllProjects(PageRequest.of(page, size)).let {
            PageableResponse(
                data = it.content,
                size = it.content.size,
                offset = page * size,
                totalCount = it.totalElements.toInt(),
                hasNext = page * size + it.size < it.totalElements
            )
        }

    @GetMapping("/project/{id}")
    @ResponseBody
    fun findProject(
        @PathVariable id: Long
    ): SuccessResponse<Project> =
        SuccessResponse(
            data = projectService.findProject(id)
        )

    @GetMapping("/project/tab")
    fun imminentProjects(): SuccessResponse<List<Project>> =
        SuccessResponse(
            data = projectService.imminentProjects()
        )

    @GetMapping("/project/search")
    @ResponseBody
    fun searchProject(
        @RequestParam keyword: String,
    ): SuccessResponse<List<Project>> =
        SuccessResponse(
            data = projectService.searchByKeyword(keyword)
        )

    @PutMapping("/project/{id}")
    @ResponseBody
    fun updateProject(): Project {
        TODO("Update는 언제?")
    }

    @DeleteMapping("/project/{id}")
    @ResponseBody
    fun deleteProject(
        @PathVariable id: Long
    ): SuccessResponse<Boolean> =
        SuccessResponse(
            data = projectService.deleteProject(id)
        )
}
