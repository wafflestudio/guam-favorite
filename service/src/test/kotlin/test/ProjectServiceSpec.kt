package waffle.guam.test

import io.kotest.matchers.collections.shouldContainAll
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
import waffle.guam.db.entity.Position
import waffle.guam.db.entity.State
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

    @DisplayName("프로젝트 생성 : ")
    @Transactional
    @Test
    fun createProject() {
        val projectId = 1
        val stacks = database.getTechStacks()
        val user = database.getUser()

        val result = projectService.createProject(
            command = DefaultCommand.CreateProject.copy(techStackIds = stacks.map { Pair(first = it.id, second = it.position) }),
            userId = user.id
        )

        result.id shouldBe projectId
        result.title shouldBe "Test Project"
        result.description shouldBe "Test Description"
        result.thumbnail shouldBe "Test Thumbnail"
        result.frontLeftCnt shouldBe 3
        result.backLeftCnt shouldBe 3
        result.designLeftCnt shouldBe 3
        result.isRecruiting shouldBe true
        result.noticeThread shouldBe null
        result.techStacks.map { it.name } shouldContainAll stacks.map { it.name }
        result.techStacks.map { it.aliases } shouldContainAll stacks.map { it.aliases }
        result.techStacks.map { it.position } shouldContainAll stacks.map { it.position }
        result.tasks!![0].projectId shouldBe projectId
        result.tasks!![0].user.id shouldBe user.id
        result.tasks!![0].user.nickname shouldBe user.nickname
        result.tasks!![0].user.status shouldBe user.status.toString()
        result.tasks!![0].task shouldBe "Let's get it started!"
        result.tasks!![0].position shouldBe Position.WHATEVER.toString()
        result.tasks!![0].state shouldBe State.LEADER
        result.due shouldBe Due.SIX
    }

    // TODO(JoinException)

    // TODO(DataNotFoundException)

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

    @DisplayName("프로젝트 단일 조회")
    @Transactional
    @Test
    fun findProject() {
        val stacks = database.getTechStacks()
        val user = database.getUser()
        val createdProject = projectService.createProject(
            command = DefaultCommand.CreateProject.copy(techStackIds = stacks.map { Pair(first = it.id, second = it.position) }),
            userId = user.id
        )

        val result = projectService.findProject(createdProject.id)

        result shouldBe createdProject
    }

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

    // TODO(search)

    // TODO(updateProject)

    // TODO(join)

    // TODO(deleteProject)

    object DefaultCommand {
        val CreateProject = CreateProject(
            title = "Test Project",
            description = "Test Description",
            thumbnail = "Test Thumbnail",
            frontLeftCnt = 3,
            backLeftCnt = 3,
            designLeftCnt = 3,
            techStackIds = emptyList(),
            due = Due.SIX
        )
    }
}
