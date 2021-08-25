package waffle.guam.api

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import waffle.guam.api.request.CreateProjectRequest
import waffle.guam.api.request.JoinProjectRequest
import waffle.guam.api.request.SearchProjectRequest
import waffle.guam.api.request.UpdateProjectRequest
import waffle.guam.api.response.PageableResponse
import waffle.guam.api.response.ProjectResponse
import waffle.guam.api.response.SuccessResponse
import waffle.guam.common.UserContext
import waffle.guam.project.ProjectService
import waffle.guam.task.TaskService
import waffle.guam.task.command.AcceptTask
import waffle.guam.task.command.ApplyTask
import waffle.guam.task.command.CancelApplyTask
import waffle.guam.task.command.CancelTask
import waffle.guam.task.command.DeclineTask
import waffle.guam.task.command.LeaveTask
import waffle.guam.task.command.TaskCommand

@RestController
@RequestMapping
class ProjectController(
    private val projectService: ProjectService,
    private val taskService: TaskService,
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

    @GetMapping("/project/{projectId}")
    fun getProject(
        @PathVariable projectId: Long,
    ): SuccessResponse<ProjectResponse> =
        SuccessResponse(
            data = ProjectResponse.of(projectService.getProject(projectId))
        )

    @GetMapping("/project/list")
    fun getAllProjects(
        @RequestParam(required = true, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "20") size: Int,
    ): PageableResponse<ProjectResponse> =
        projectService.getAllProjects(
            PageRequest.of(page, size, Sort.by("modifiedAt").descending())
        ).let {
            PageableResponse(
                data = it.content.map { prj -> ProjectResponse.of(prj) },
                size = it.content.size,
                offset = page * size,
                totalCount = it.totalElements.toInt(),
                hasNext = page * size + it.size < it.totalElements
            )
        }

    @GetMapping("/project/search")
    fun searchProjects(
        @RequestParam(required = true, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "20") size: Int,
        searchProjectRequest: SearchProjectRequest,
    ): PageableResponse<ProjectResponse> =
        projectService.getSearchResults(
            PageRequest.of(page, size, Sort.by("modifiedAt").descending()),
            searchProjectRequest.toCommand()
        ).let {
            PageableResponse(
                data = it.content.map { prj -> ProjectResponse.of(prj) },
                size = it.content.size,
                offset = page * size,
                totalCount = it.totalElements.toInt(),
                hasNext = page * size + it.size < it.totalElements
            )
        }

    @GetMapping("/project/tab")
    fun getTabProjects(
        @RequestParam(required = true, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "20") size: Int,
    ): PageableResponse<ProjectResponse> =
        projectService.getTabProjects(
            PageRequest.of(page, size, Sort.by("modifiedAt").descending())
        ).let {
            PageableResponse(
                data = it.content.map { prj -> ProjectResponse.of(prj) },
                size = it.content.size,
                offset = page * size,
                totalCount = it.totalElements.toInt(),
                hasNext = page * size + it.size < it.totalElements
            )
        }

    @PostMapping("/project/{projectId}")
    fun joinProject(
        @PathVariable projectId: Long,
        @RequestBody joinProject: JoinProjectRequest,
        userContext: UserContext,
    ): SuccessResponse<Unit> =
        taskService.handle(
            command = ApplyTask(
                userId = userContext.id,
                projectId = projectId,
                position = joinProject.position,
                introduction = joinProject.introduction
            )
        ).run {
            SuccessResponse(Unit)
        }

    @DeleteMapping("/project/{projectId}")
    fun deleteProject(
        @PathVariable projectId: Long,
        userContext: UserContext,
    ): SuccessResponse<Unit> =
        projectService.deleteProject(projectId, userContext.id).run {
            SuccessResponse(Unit)
        }

    @PostMapping("/project/{projectId}/{userId}")
    fun acceptJoinOrNot(
        @PathVariable projectId: Long,
        @PathVariable userId: Long,
        @RequestParam accept: Boolean,
        userContext: UserContext,
    ): SuccessResponse<Unit> =
        taskService.handle(
            if (accept)
                AcceptTask(leaderId = userContext.id, guestId = userId, projectId = projectId)
            else
                DeclineTask(leaderId = userContext.id, guestId = userId, projectId = projectId)
        ).run {
            SuccessResponse(Unit)
        }

    @PostMapping("/project/{projectId}/edit")
    fun updateProject(
        @PathVariable projectId: Long,
        updateRequest: UpdateProjectRequest,
        userContext: UserContext,
    ): SuccessResponse<Unit> =
        projectService.updateProject(updateRequest.toCommand(), projectId, userContext.id).run {
            SuccessResponse(Unit)
        }

    @PostMapping("/project/{projectId}/quit")
    fun quitProject(
        @PathVariable projectId: Long,
        userContext: UserContext,
    ): SuccessResponse<Unit> =
        taskService.handle(
            command = getQuitCommand(projectId = projectId, userId = userContext.id)
        ).run {
            SuccessResponse(Unit)
        }

    private fun getQuitCommand(projectId: Long, userId: Long): TaskCommand =
        if (taskService.isLeader(projectId = projectId, userId = userId)) {
            println("is Leader")
            CancelTask(projectId = projectId)
        } else if (taskService.isMember(projectId = projectId, userId = userId)) {
            println("is Member")
            LeaveTask(projectId = projectId, userId = userId)
        } else {
            println("is Guest or Nothing")
            CancelApplyTask(projectId = projectId, userId = userId)
        }
}
