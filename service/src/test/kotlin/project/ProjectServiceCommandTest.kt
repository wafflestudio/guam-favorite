package waffle.guam.project

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import waffle.guam.annotation.DatabaseTest
import waffle.guam.comment.CommentRepository
import waffle.guam.project.command.UpdateProject
import waffle.guam.project.model.ProjectState
import waffle.guam.projectstack.PrjStackServiceImpl
import waffle.guam.projectstack.ProjectStackRepository
import waffle.guam.stack.StackRepository
import waffle.guam.task.TaskCandidateRepository
import waffle.guam.task.TaskHandler
import waffle.guam.task.TaskHistoryRepository
import waffle.guam.task.TaskRepository
import waffle.guam.task.TaskServiceImpl
import waffle.guam.thread.ThreadRepository
import waffle.guam.thread.ThreadServiceImpl
import waffle.guam.thread.ThreadViewRepository
import waffle.guam.user.UserRepository

@DatabaseTest(["project/image.sql", "project/project.sql", "project/stack.sql", "project/user.sql", "project/task.sql", "project/projectStack.sql"])
class ProjectServiceCommandTest @Autowired constructor(
    private val taskRepository: TaskRepository,
    private val taskCandidateRepository: TaskCandidateRepository,
    private val taskHistoryRepository: TaskHistoryRepository,
    private val userRepository: UserRepository,
    private val projectRepository: ProjectRepository,
    private val threadRepository: ThreadRepository,
    private val threadViewRepository: ThreadViewRepository,
    private val commentRepository: CommentRepository,
    private val projectStackRepository: ProjectStackRepository,
    private val stackRepository: StackRepository
) {

    private val taskHandler = TaskHandler(
        taskRepository,
        taskCandidateRepository,
        taskHistoryRepository,
        userRepository,
        projectRepository,
    )

    private val taskService = TaskServiceImpl(
        taskRepository,
        taskCandidateRepository,
        taskHandler,
    )

    private val threadService = ThreadServiceImpl(
        threadRepository,
        threadViewRepository,
        projectRepository,
        taskService,
        commentRepository
    )

    private val projectStackService = PrjStackServiceImpl(
        projectStackRepository,
        stackRepository
    )

    private val projectService: ProjectService = ProjectServiceImpl(
        projectStackService, taskService, threadService, projectRepository
    )

    /**
     * Note
     * 대수술 이후
     * 승인 및 반려, 나가기 등  작업은 task 도메인으로 움직인 듯 보입니다.
     * 그런고로,, prj 도메인에서는 테스트를 포기하겠습니다..
     * 믿습니다.
     * (머지 승인 후 삭제 요망)
     */

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
