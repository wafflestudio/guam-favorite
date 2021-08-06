package waffle.guam.project_stack

import waffle.guam.project_stack.command.StackIdList
import waffle.guam.project_stack.event.ProjectStacksCreated
import waffle.guam.project_stack.event.ProjectStacksUpdated
import waffle.guam.project_stack.model.ProjectStack

interface ProjectStackService {

    /**
     * NOTE
     * 이 서비스는 컨트롤러랑 붙어 있지 않고, prj 서비스를 이어받아 작업을 완수하는 용도로만 쓰입니다.
     */

    // READ
    fun getProjectStacks(projectId: Long): ProjectStack
    fun getAllProjectStacks(projectIds: Long): List<ProjectStack>

    // CREATE
    fun createProjectStacks(projectId: Long, command: StackIdList): ProjectStacksCreated

    // PUT
    fun updateProjectStacks(projectId: Long, command: StackIdList): ProjectStacksUpdated

    // DELETE -> 해당 메소드를 호출하는 곳이 현재로서는 없습니다.
    // fun deleteProject(projectId: Long, userId: Long): ProjectDeleted
}
