package waffle.guam.project

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import waffle.guam.project.command.CreateProject
import waffle.guam.project.command.SearchProject
import waffle.guam.project.command.UpdateProject
import waffle.guam.project.event.ProjectCompleted
import waffle.guam.project.event.ProjectCreated
import waffle.guam.project.event.ProjectDeleted
import waffle.guam.project.event.ProjectUpdated
import waffle.guam.project.model.Project

interface ProjectService {

    // READ - (ProjectStackService, TaskService DI)
    fun getProject(projectId: Long): Project
    fun getAllProjects(pageable: Pageable): Page<Project>
    fun getTabProjects(pageable: Pageable): Page<Project>
    fun getSearchResults(pageable: Pageable, command: SearchProject): Page<Project>

    // CREATE - (ImageService DI) -> ProjectStackService -> TaskService
    fun createProject(command: CreateProject, userId: Long): ProjectCreated

    // UPDATE - (ImageService DI) -> ProjectStackService (taskService 필요 없다)
    fun updateProject(command: UpdateProject, projectId: Long, userId: Long): ProjectUpdated

    // DELETE
    fun deleteProject(projectId: Long, userId: Long): ProjectDeleted

    // COMPLETE (??? request)
    fun completeProject(projectId: Long, userId: Long): ProjectCompleted
}
