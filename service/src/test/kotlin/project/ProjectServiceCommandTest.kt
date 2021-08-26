package waffle.guam.project

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import waffle.guam.annotation.DatabaseTest
import waffle.guam.project.command.UpdateProject
import waffle.guam.project.model.ProjectState
import waffle.guam.projectstack.ProjectStackService
import waffle.guam.task.TaskService
import waffle.guam.task.command.AcceptTask
import waffle.guam.task.command.DeclineTask
import waffle.guam.task.command.LeaveTask
import waffle.guam.task.model.UserState
import waffle.guam.task.query.SearchTask

@DatabaseTest(["project/image.sql", "project/project.sql", "project/user.sql", "project/task.sql", "project/projectStack.sql"])
class ProjectServiceCommandTest @Autowired constructor(
    private val projectStackService: ProjectStackService,
    private val taskService: TaskService,
    private val projectRepository: ProjectRepository
) {

    val projectService: ProjectService = ProjectServiceImpl(
        projectStackService, taskService, threadService, projectRepository
    )

    @DisplayName("프로젝트 나가기")
    @Transactional
    @Test
    fun leaveProject() {

        val event = taskService.handle(
            command = LeaveTask(
                2, 1
            )
        )

        val tasks = taskService.getTasks(
            command = SearchTask.taskQuery().projectIds(1)
        )

        tasks.size shouldBe 2
    }

    @DisplayName("프로젝트 승인")
    @Transactional
    @Test
    fun acceptOrNot() {

        val event = taskService.handle(
            command = AcceptTask(
                1, 2, 1
            )
        )

        val task = taskService.getTask(
            command = SearchTask.taskQuery().userIds(2)
        )

        task.userState shouldBe UserState.MEMBER
        // TODO offset 이해가 아직 안됨..ㅜ
    }

    @DisplayName("프로젝트 반려")
    @Transactional
    @Test
    fun accept_Not() {

        val event = taskService.handle(
            command = DeclineTask(
                1, 2, 1
            )
        )

        val task = taskService.getTask(
            command = SearchTask.taskQuery().projectIds(1).userIds(2)
        )

        // FIXME
        //  task.userState shouldBe UserState.DECLINED
    }

    @DisplayName("프로젝트 업데이트")
    @Transactional
    @Test
    fun update() {

        val event = projectService.updateProject(
            command = UpdateProject(
                title = "엄",
                description = "기본값 설정 해둘걸",
                frontHeadCnt = 4,
                backHeadCnt = 4,
                designHeadCnt = 4,
                imageFiles = null,
                frontStackId = 2,
                backStackId = 15,
                designStackId = 34,
                due = null
            ),
            projectId = 1,
            userId = 1
        )

        val res = projectService.getProject(1)

        res.title shouldBe "엄"
    }

    @DisplayName("프로젝트 삭제")
    @Transactional
    @Test
    fun delete() {

        val event = projectService.deleteProject(1, 1)

        val res = projectService.getProject(1)

        res.state shouldBe ProjectState.CLOSED
    }
}
