package waffle.guam.test

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.transaction.annotation.Transactional
import waffle.guam.Database
import waffle.guam.DatabaseTest
import waffle.guam.db.entity.Due
import waffle.guam.db.repository.CommentRepository
import waffle.guam.db.repository.ProjectRepository
import waffle.guam.db.repository.ProjectStackRepository
import waffle.guam.db.repository.ProjectViewRepository
import waffle.guam.db.repository.TaskRepository
import waffle.guam.db.repository.ThreadViewRepository
import waffle.guam.service.ChatService
import waffle.guam.service.ProjectService
import waffle.guam.service.command.CreateProject

@DatabaseTest
class ProjectServiceSpec @Autowired constructor(
    private val projectRepository: ProjectRepository,
    private val projectViewRepository: ProjectViewRepository,
    private val projectStackRepository: ProjectStackRepository,
    private val taskRepository: TaskRepository,
    private val chatService: ChatService,
    private val threadViewRepository: ThreadViewRepository,
    private val commentRepository: CommentRepository,
    private val database: Database,
) {

    private val projectService = ProjectService(
        projectRepository = projectRepository,
        projectViewRepository = projectViewRepository,
        projectStackRepository = projectStackRepository,
        taskRepository = taskRepository,
        chatService = chatService,
        threadViewRepository = threadViewRepository,
        commentRepository = commentRepository
    )

    @BeforeEach
    fun clearDatabase() {
        database.cleanUp()
    }

//    @DisplayName("프로젝트 생성")
//    @Transactional
//    @Test
//    fun createProject() {
//        val stacks = database.getTechStacks()
//        val user = database.getUser()
//
//        val result = projectService.createProject(
//            command = DefaultCommand.CreateProject.copy(techStackIds = stacks.map { it.id }),
//            userId = user.id
//        )
//
//        result.title shouldBe "Test Project"
//        result.frontLeftCnt shouldBe 3
//        result.techStacks.map { it.name } shouldContainAll stacks.map { it.name }
//        result.tasks!![0].id shouldBe user.id
//    }

    // TODO(JoinException)

    @DisplayName("프로젝트 전체 목록 조회")
    @Transactional
    @Test
    fun getAllProject() {
        val user = database.getUser()
        val totalAmount = 3
        val page = 2
        val size = 1

        for (i in 0 until totalAmount) {
            projectService.createProject(command = DefaultCommand.CreateProject, userId = user.id)
        }

        val result = projectService.getAllProjects(pageable = PageRequest.of(page, size))

        result.totalElements shouldBe totalAmount
        result.content.size shouldBe 1
        // 전체 조회시, 구성원 정보는 생략한다.
        result.content.forEach { it.tasks shouldBe null }
    }

//    @DisplayName("프로젝트 단일 조회")
//    @Transactional
//    @Test
//    fun findProject() {
//        val stacks = database.getTechStacks()
//        val user = database.getUser()
//        val createdProject = projectService.createProject(
//            command = DefaultCommand.CreateProject.copy(techStackIds = stacks.map { it.id }),
//            userId = user.id
//        )
//
//        val result = projectService.findProject(createdProject.id)
//
//        result shouldBe createdProject
//    }

    @DisplayName("마감 임박 프로젝트 목록 조회")
    @Transactional
    @Test
    fun imminentProjects() {
        val user = database.getUser()

        for (i in 0 until 3) {
            projectService.createProject(
                command = DefaultCommand.CreateProject.copy(frontLeftCnt = i),
                userId = user.id,
            )
        }

        val result = projectService.imminentProjects()

        result.size shouldBe 2
    }

    object DefaultCommand {
        val CreateProject = CreateProject(
            title = "Test Project",
            description = "This is test",
            thumbnail = "This is test",
            frontLeftCnt = 3,
            backLeftCnt = 3,
            designLeftCnt = 3,
            techStackIds = emptyList(),
            due = Due.SIX
        )
    }
}
