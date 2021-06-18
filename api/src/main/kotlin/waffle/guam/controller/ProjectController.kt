package waffle.guam.controller

import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import waffle.guam.controller.response.GuamResponse
import waffle.guam.controller.response.SuccessResponse
import waffle.guam.model.Project
import waffle.guam.service.ProjectService
import waffle.guam.service.command.CreateProject

@RestController
@RequestMapping
class ProjectController(
    private val projectService: ProjectService,
) {

    @PostMapping("/projects")
    @ResponseBody
    fun createProject(
        @RequestBody createProject: CreateProject,
        @RequestHeader("USER-ID") id: Long
    ): GuamResponse =
        SuccessResponse(
            data = projectService.createProject(createProject, id)
        )

    @GetMapping("/projects")
    @ResponseBody
    fun getAllProjects(
        @PageableDefault(size = 10, sort = ["id"], direction = Sort.Direction.DESC) pageable: Pageable
    ): GuamResponse =
        SuccessResponse(
            data = projectService.getAllProjects(pageable)
        )

    @GetMapping("/projects/{id}")
    @ResponseBody
    fun findProject(
        @PathVariable id: Long
    ): GuamResponse =
        SuccessResponse(
            data = projectService.findProject(id)
        )

    @GetMapping("/projects/tab")
    fun imminentProjects(): GuamResponse =
        SuccessResponse(
            data = projectService.imminentProjects()
        )

    @GetMapping("/projects/search")
    @ResponseBody
    fun searchProject(
        @RequestParam keyword: String,
    ): GuamResponse =
        SuccessResponse(
            data = projectService.searchByKeyword(keyword)
        )

    @PutMapping("/projects/{id}")
    @ResponseBody
    fun updateProject(): Project {
        TODO("Update는 언제?")
    }

    @DeleteMapping("/projects/{id}")
    @ResponseBody
    fun deleteProject(
        @PathVariable id: Long
    ): GuamResponse =
        SuccessResponse(
            data = projectService.deleteProject(id)
        )
}
