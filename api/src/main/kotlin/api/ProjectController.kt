package waffle.guam.api

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import waffle.guam.api.request.CreateProjectRequest
import waffle.guam.api.response.PageableResponse
import waffle.guam.api.response.ProjectResponse
import waffle.guam.api.response.SuccessResponse
import waffle.guam.common.UserContext
import waffle.guam.project.ProjectService

@RestController
@RequestMapping
class ProjectController(
    private val projectService: ProjectService,
) {

    @PostMapping("/project")
    fun createProject(
        createProject: CreateProjectRequest,
        userContext: UserContext,
    ): SuccessResponse<Unit> =
        projectService.createProject(
            command = createProject.toCommand(),
            userId = userContext.id
        ).let {
            SuccessResponse(Unit)
        }

    /***
     * (1) select task t from tasks t inner join users u outer join images i
     * (2) select project outer join images
     * (3) insert project
     * (4) select stack X 3 (front, back, designer)
     * (5) insert project_stacks X 3 (front, back, designer)
     * (6) select project outer join images
     * (7) insert tasks
     */

    @GetMapping("/project/list")
    fun getAllProjects(
        @RequestParam(required = true, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "20") size: Int,
    ): PageableResponse<ProjectResponse> =
        projectService.getAllProjects(
            PageRequest.of(page, size, Sort.by("modifiedAt").descending())
        ).let {
            PageableResponse(
                data = it.content.map { ProjectResponse.of(it) },
                size = it.content.size,
                offset = page * size,
                totalCount = it.totalElements.toInt(),
                hasNext = page * size + it.size < it.totalElements
            )
        }

    /**
     * (1) select project outer join images
     * (2) select project_stack inner join tech_stack
     * (3) select task inner join users outer join images
     */
}
