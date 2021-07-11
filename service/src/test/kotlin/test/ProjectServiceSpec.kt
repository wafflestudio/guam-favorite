package waffle.guam.test

import io.kotest.assertions.throwables.shouldThrowExactly
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
import waffle.guam.DefaultDataInfo
import waffle.guam.db.entity.Due
import waffle.guam.db.entity.Position
import waffle.guam.db.entity.State
import waffle.guam.db.repository.CommentRepository
import waffle.guam.db.repository.ProjectRepository
import waffle.guam.db.repository.ProjectStackRepository
import waffle.guam.db.repository.ProjectViewRepository
import waffle.guam.db.repository.TaskRepository
import waffle.guam.db.repository.ThreadViewRepository
import waffle.guam.exception.DataNotFoundException
import waffle.guam.exception.JoinException
import waffle.guam.service.ChatService
import waffle.guam.service.ProjectService
import waffle.guam.service.command.CreateProject
import java.time.LocalDateTime
// import java.util.Optional

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

    @DisplayName("프로젝트 생성 : 주어진 스택 정보로 새로운 project와 projectStack들 생성하여 정보 반환")
    @Transactional
    @Test
    fun createProjectOK() {
        val projectId = 1
        val stacks = database.getTechStacks()
        val user = database.getUser()

        val result = projectService.createProject(
            command = DefaultCommand.CreateProject.copy(techStackIds = stacks.map { Pair(first = it.id, second = it.position) }),
            userId = user.id
        )
        val dbProjectStacks = projectStackRepository.findAll()
        val dbTasks = taskRepository.findAll()

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
        dbProjectStacks[0].projectId shouldBe 1
        dbProjectStacks[1].projectId shouldBe 1
        dbTasks[0].projectId shouldBe 1
        dbTasks[0].userId shouldBe user.id
    }

    @DisplayName("프로젝트 생성 : 이미 3개의 프로젝트에 참여 중인 사용자가 새로운 프로젝트 생성 시도시 예외가 발생한다")
    @Transactional
    @Test
    fun createProjectJoinFailException() {
        val user = database.getUser()

        for (i in 0 until 3) {
            projectService.createProject(command = DefaultCommand.CreateProject.copy(), userId = user.id)
        }

        shouldThrowExactly<JoinException> {
            projectService.createProject(command = DefaultCommand.CreateProject.copy(), userId = user.id)
        }
    }

    // TODO(createProjectCreatedProjectFoundException : 논리적으로 발생 불가? mock 혹은 spy 필요?)

    @DisplayName("프로젝트 전체 목록 조회 :  page, size 정보에 맞게 복수의 프로젝트들을 조회할 수 있다")
    @Transactional
    @Test
    fun getAllProjectOK() {
        val users = database.getUsers()
        val stacks = database.getTechStacks()
        val totalAmount = 9
        val page = 1
        val size = 4

        for (i in 0 until totalAmount) {
            projectService.createProject(
                command = DefaultCommand.CreateProject.copy(
                    title = "Project $i",
                    techStackIds = stacks.map { Pair(first = it.id, second = it.position) }
                ),
                userId = users[i % 3].id
            )
        }

        val result = projectService.getAllProjects(pageable = PageRequest.of(page, size))

        result.content.size shouldBe size
        result.content.forEach { it.tasks shouldBe null }
        result.content.forEach { project -> project.techStacks.map { it.name } shouldContainAll stacks.map { it.name } }
        result.content.forEach { project -> project.techStacks.map { it.aliases } shouldContainAll stacks.map { it.aliases } }
        result.content.forEach { project -> project.techStacks.map { it.position } shouldContainAll stacks.map { it.position } }
        result.content[0].title shouldBe "Project ${page * size}"
        result.content[1].title shouldBe "Project ${page * size + 1}"
        result.pageable.offset shouldBe page * size
        result.totalElements shouldBe totalAmount
    }

    @DisplayName("프로젝트 전체 목록 조회 : page와 size에 해당되는 범위에 프로젝트가 없어도 예외는 발생하지 않는다")
    @Transactional
    @Test
    fun getAllProjectOutOfRangeOK() {
        val users = database.getUsers()
        val totalAmount = 5
        val page = 100
        val size = 100

        for (i in 0 until totalAmount) {
            projectService.createProject(
                command = DefaultCommand.CreateProject,
                userId = users[i % 3].id
            )
        }

        val result = projectService.getAllProjects(pageable = PageRequest.of(page, size))

        result.content.size shouldBe 0
        result.content shouldBe listOf()
        result.pageable.pageNumber shouldBe page
        result.pageable.pageSize shouldBe size
        result.pageable.offset shouldBe page * size
        result.totalElements shouldBe totalAmount
    }

    @DisplayName("단일 프로젝트 세부 조회 : id에 해당하는 프로젝트의 작업실 세부 정보 조회 가능 (task, techStack 포함)")
    @Transactional
    @Test
    fun findProjectOK() {
        val stacks = database.getTechStacks()
        val user = database.getUser()
        val createdProject = projectService.createProject(
            command = DefaultCommand.CreateProject.copy(techStackIds = stacks.map { Pair(first = it.id, second = it.position) }),
            userId = user.id
        )
        val result = projectService.findProject(createdProject.id)

        result.title shouldBe createdProject.title
        result.description shouldBe createdProject.description
        result.thumbnail shouldBe createdProject.thumbnail
        result.frontLeftCnt shouldBe createdProject.frontLeftCnt
        result.isRecruiting shouldBe createdProject.isRecruiting
        result.techStacks.map { it.name } shouldContainAll stacks.map { it.name }
        result.techStacks.map { it.aliases } shouldContainAll stacks.map { it.aliases }
        result.techStacks.map { it.position } shouldContainAll stacks.map { it.position }
        result.tasks!![0].user.id shouldBe user.id
        result.tasks!![0].user.status shouldBe user.status.toString()
        result.tasks!![0].user.nickname shouldBe user.nickname
        result.tasks!![0].state shouldBe State.LEADER
        result.tasks!![0].position shouldBe Position.WHATEVER.toString()
        result.tasks!![0].projectId shouldBe createdProject.id
        result.due shouldBe createdProject.due
        result.noticeThread shouldBe null
    }

    @DisplayName("단일 프로젝트 세부 조회 : 공지쓰레드가 있는 경우 포함하여 작업실 정보 조회 가능")
    @Transactional
    @Test
    fun findProjectWithNoticeThreadOK() {
        val user = database.getUser()
        val thread = database.getThread()
        database.getComment()
        val createdProject = projectRepository.save(
            DefaultDataInfo.project.copy(
                noticeThreadId = thread.id, modifiedAt = LocalDateTime.now()
            )
        )
        val result = projectService.findProject(createdProject.id)

        result.noticeThread?.id shouldBe thread.id
        result.noticeThread?.content shouldBe thread.content
        result.noticeThread?.creatorId shouldBe thread.userId
        result.noticeThread?.creatorNickname shouldBe user.nickname
        result.title shouldBe createdProject.title
        result.isRecruiting shouldBe createdProject.recruiting
        result.description shouldBe createdProject.description
    }

    @DisplayName("프로젝트 단일 조회 : id에 해당하는 프로젝트가 없는 경우 예외가 발생한다")
    @Transactional
    @Test
    fun findProjectProjectNotFoundException() {
        shouldThrowExactly<DataNotFoundException> {
            projectService.findProject(9999999999999)
        }
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

    // TODO(searchOK)

    // TODO(updateProjectOK)

    // TODO(joinOK)

    // TODO(ProjectEntity remaining problem - only in test logic)
    @DisplayName("프로젝트 삭제 : id에 해당되는 프로젝트 1개만 삭제한다.")
    @Transactional
    @Test
    fun deleteProjectOK() {
        val projectId = 2L
        val user = database.getUser()
        for (i in 0 until 3) {
            projectService.createProject(command = DefaultCommand.CreateProject, userId = user.id)
        }
        val dbExistingProjects = projectRepository.findAll()
        val result = projectService.deleteProject(projectId)
        // database.flush()
        val remainingProjects = projectRepository.findAll()
        // val dbDeletedProject = projectRepository.findById(projectId)

        result shouldBe true
        dbExistingProjects.size shouldBe 3
        remainingProjects.size shouldBe 2
        // dbDeletedProject shouldBe Optional.empty()
    }

    @DisplayName("프로젝트 삭제 : 프로젝트에 연결된 task와 projectStack을 함께 삭제한다.")
    @Transactional
    @Test
    fun deleteProjectWithTasksAndProjectStacksOK() {
        val projectId = 2L
        val stacks = database.getTechStacks()
        val user = database.getUser()
        for (i in 0 until 3) {
            projectService.createProject(
                command = DefaultCommand.CreateProject.copy(
                    techStackIds = stacks.map {
                        Pair(
                            first = it.id,
                            second = it.position
                        )
                    }
                ),
                userId = user.id
            )
        }
        val dbExistingProjectStacks = projectStackRepository.findAll()
        val dbExistingTasks = taskRepository.findAll()
        val dbExistingProjects = projectRepository.findAll()

        val result = projectService.deleteProject(projectId)

        val dbRemainingProjectStacks = projectStackRepository.findAll()
        val dbRemainingTasks = taskRepository.findAll()
        val remainingProjects = projectRepository.findAll()

        result shouldBe true
        dbExistingProjects.size shouldBe 3
        remainingProjects.size shouldBe 2
        dbExistingProjectStacks.size shouldBe 6
        dbRemainingProjectStacks.size shouldBe 4
        dbExistingTasks.size shouldBe 3
        dbRemainingTasks.size shouldBe 2
    }

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
