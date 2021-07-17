package waffle.guam.test

import io.kotest.assertions.throwables.shouldNotThrowExactly
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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
import waffle.guam.db.repository.TaskViewRepository
import waffle.guam.db.repository.ThreadViewRepository
import waffle.guam.exception.DataNotFoundException
import waffle.guam.exception.JoinException
import waffle.guam.exception.NotAllowedException
import waffle.guam.service.ChatService
import waffle.guam.service.ProjectService
import waffle.guam.service.command.CreateProject
import waffle.guam.service.command.DeleteThread
import waffle.guam.service.command.SetNoticeThread
import java.util.Optional

@DatabaseTest
class ProjectServiceSpec @Autowired constructor(
    private val projectRepository: ProjectRepository,
    private val projectViewRepository: ProjectViewRepository,
    private val projectStackRepository: ProjectStackRepository,
    private val taskViewRepository: TaskViewRepository,
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
        taskViewRepository = taskViewRepository,
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
        database.flushAndClear()

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
        result.tasks!![0].user.status shouldBe user.status.name
        result.tasks!![0].position shouldBe Position.WHATEVER.name
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
    fun createProjectJoinLimitException() {
        val user = database.getUser()

        for (i in 0 until 3) {
            projectService.createProject(command = DefaultCommand.CreateProject.copy(), userId = user.id)
        }

        shouldThrowExactly<JoinException> {
            projectService.createProject(command = DefaultCommand.CreateProject.copy(), userId = user.id)
        }
    }

    @DisplayName("프로젝트 생성 : 생성하려는 프로젝트에서 자신의 Position을 선택하지 않는 경우 예외가 발생한다")
    @Transactional
    @Test
    fun createProjectNullPositionException() {
        val user = database.getUser()

        shouldThrowExactly<JoinException> {
            projectService.createProject(command = DefaultCommand.CreateProject.copy(myPosition = null), userId = user.id)
        }
    }

    // TODO(createProjectCreatedProjectFoundException : 생성한 프로젝트가 DB에 생성되지 않은 경우에 대한 예외처리 - 논리적으로 발생 불가? mock or spy?)

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

    @DisplayName("단일 프로젝트 세부 조회 : 특정 프로젝트의 작업실 세부 정보와 연결된 task, techStack 조회 가능")
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
        database.getImages()
        database.getComment()
        val createdProject = projectService.createProject(
            command = DefaultCommand.CreateProject,
            userId = user.id
        )
        chatService.setNoticeThread(
            command = SetNoticeThread(
                projectId = createdProject.id,
                threadId = thread.id,
                userId = user.id
            )
        )
        database.flushAndClear()
        val result = projectService.findProject(createdProject.id)

        result.title shouldBe createdProject.title
        result.isRecruiting shouldBe createdProject.isRecruiting
        result.description shouldBe createdProject.description
        result.noticeThread?.id shouldBe thread.id
        result.noticeThread?.content shouldBe thread.content
        result.noticeThread?.creatorId shouldBe thread.userId
        result.noticeThread?.creatorNickname shouldBe user.nickname
    }

    @DisplayName("단일 프로젝트 세부 조회 : noticeThreadId에 해당되는 공지 쓰레드가 삭제된 경우 예외가 발생하지 않는다")
    @Transactional
    @Test
    fun findProjectNoticeThreadNotFoundOK() {
        val user = database.getUser()
        val thread = database.getThread()
        database.getImages()
        database.getComment()
        val createdProject = projectService.createProject(
            command = DefaultCommand.CreateProject,
            userId = user.id
        )
        chatService.setNoticeThread(
            command = SetNoticeThread(
                projectId = createdProject.id,
                threadId = thread.id,
                userId = user.id
            )
        )
        database.flushAndClear()
        val prevResult = projectService.findProject(createdProject.id)

        chatService.deleteThread(
            command = DeleteThread(
                threadId = thread.id,
                userId = user.id
            )
        )
        database.flushAndClear()
        val threadDeletedResult = projectService.findProject(createdProject.id)

        prevResult.noticeThread?.id shouldBe thread.id
        prevResult.noticeThread?.content shouldBe thread.content
        prevResult.noticeThread?.creatorId shouldBe thread.userId

        threadDeletedResult.title shouldBe createdProject.title
        threadDeletedResult.isRecruiting shouldBe createdProject.isRecruiting
        threadDeletedResult.description shouldBe createdProject.description
        threadDeletedResult.noticeThread shouldBe null
    }

    @DisplayName("프로젝트 단일 조회 : id에 해당하는 프로젝트가 없는 경우 예외가 발생한다")
    @Transactional
    @Test
    fun findProjectProjectNotFoundException() {
        shouldThrowExactly<DataNotFoundException> {
            projectService.findProject(9999999999999)
        }
    }

    @DisplayName("리크루팅 마감 임박 프로젝트 목록 조회 : 특정 포지션 모집인원이 1명 남은 프로젝트들을 모두 조회할 수 있다")
    @Transactional
    @Test
    fun imminentProjectsOK() {
        val users = database.getUsers()
        for (i in 0 until 3) {
            projectService.createProject(
                command = DefaultCommand.CreateProject.copy(
                    title = "Need Everything",
                    frontLeftCnt = 10,
                    backLeftCnt = 10,
                    designLeftCnt = 10,
                ),
                userId = users[0].id
            )
        }
        for (i in 0 until 3) {
            projectService.createProject(
                command = DefaultCommand.CreateProject.copy(
                    title = "Need Only 1 Frontend",
                    frontLeftCnt = 1,
                ),
                userId = users[1].id
            )
        }
        for (i in 0 until 3) {
            projectService.createProject(
                command = DefaultCommand.CreateProject.copy(
                    title = "Need Only 1 Designer",
                    backLeftCnt = 1,
                ),
                userId = users[2].id
            )
        }

        val result = projectService.imminentProjects()

        result.size shouldBe 6
        result.map { it.title } shouldContainAll listOf("Need Only 1 Frontend", "Need Only 1 Designer")
        result.forEach { it.tasks shouldBe null }
    }

    @DisplayName("리크루팅 마감 임박 프로젝트 목록 조회 : 마감 직전의 프로젝트가 없어도 예외는 발생하지 않는다")
    @Transactional
    @Test
    fun imminentProjectsNoResultOK() {
        val users = database.getUsers()
        for (i in 0 until 9) {
            projectService.createProject(
                command = DefaultCommand.CreateProject.copy(
                    title = "Need Everything",
                    frontLeftCnt = 10,
                    backLeftCnt = 10,
                    designLeftCnt = 10,
                ),
                userId = users[i % 3].id
            )
        }
        val result = projectService.imminentProjects()

        result.size shouldBe 0
    }

    @DisplayName("프로젝트 검색 : 검색어에 해당되는 프로젝트들만 조회한다")
    @Transactional
    @Test
    fun searchOK() {
        val users = database.getUsers()
        for (i in 0 until 3) {
            projectService.createProject(
                command = DefaultCommand.CreateProject.copy(title = "Project $i"),
                userId = users[0].id
            )
            projectService.createProject(
                command = DefaultCommand.CreateProject.copy(title = "프로젝트 $i"),
                userId = users[1].id
            )
        }
        val result = projectService.search(
            query = "Proj",
            due = null,
            stackId = null,
            position = null
        )
        result.size shouldBe 3
    }

    @DisplayName("프로젝트 검색 : 한국어 검색어에 해당되는 프로젝트들만 조회한다")
    @Transactional
    @Test
    fun searchByKoreanOK() {
        val users = database.getUsers()
        for (i in 0 until 3) {
            projectService.createProject(
                command = DefaultCommand.CreateProject.copy(title = "Project $i"),
                userId = users[0].id
            )
            projectService.createProject(
                command = DefaultCommand.CreateProject.copy(title = "프로젝트 $i"),
                userId = users[1].id
            )
        }
        val result = projectService.search(
            query = "프",
            due = null,
            stackId = null,
            position = null
        )
        result.size shouldBe 3
    }

    @DisplayName("프로젝트 검색 : 숫자 검색어에 해당되는 프로젝트들만 조회한다")
    @Transactional
    @Test
    fun searchByNumberOK() {
        val users = database.getUsers()
        for (i in 0 until 3) {
            projectService.createProject(
                command = DefaultCommand.CreateProject.copy(title = "Project $i"),
                userId = users[0].id
            )
            projectService.createProject(
                command = DefaultCommand.CreateProject.copy(title = "프로젝트 $i"),
                userId = users[1].id
            )
        }
        val result = projectService.search(
            query = "1",
            due = null,
            stackId = null,
            position = null
        )
        result.size shouldBe 2
    }

    @DisplayName("프로젝트 검색 : 검색어와 프로젝트 예상 기간 정보에 해당되는 프로젝트들만 조회한다")
    @Transactional
    @Test
    fun searchWithDueOK() {
        val users = database.getUsers()
        for (i in 0 until 3) {
            projectService.createProject(
                command = DefaultCommand.CreateProject.copy(title = "Project $i", due = Due.ONE),
                userId = users[0].id
            )
            projectService.createProject(
                command = DefaultCommand.CreateProject.copy(title = "프로젝트 $i", due = Due.SIX),
                userId = users[1].id
            )
        }
        val result1 = projectService.search(
            query = "프",
            due = Due.ONE,
            stackId = null,
            position = null
        )
        val result2 = projectService.search(
            query = "2",
            due = Due.SIX,
            stackId = null,
            position = null
        )
        result1.size shouldBe 0
        result2.size shouldBe 1
    }

    @DisplayName("프로젝트 검색 : 검색어에 해당되는 프로젝트가 없어도 예외는 발생하지 않는다")
    @Transactional
    @Test
    fun searchNoResultOK() {
        val users = database.getUsers()
        for (i in 0 until 9) {
            projectService.createProject(
                command = DefaultCommand.CreateProject,
                userId = users[i % 3].id
            )
        }
        val result = projectService.search(
            query = "AAA",
            due = null,
            stackId = null,
            position = null
        )

        result.size shouldBe 0
    }

    @DisplayName("프로젝트 수정: 해당 Project 정보들이 수정된다")
    @Transactional
    @Test
    fun updateProjectOK() {
        val user = database.getUser()
        val prevProject = projectService.createProject(
            command = DefaultCommand.CreateProject.copy(
                title = "Previous Title",
                description = "Should be the Same",
                techStackIds = listOf()
            ),
            userId = user.id
        ).copy()

        val result = projectService.updateProject(
            id = prevProject.id,
            command = CreateProject(
                title = "Changed Title",
                description = prevProject.description,
                thumbnail = prevProject.thumbnail,
                frontLeftCnt = prevProject.frontLeftCnt,
                backLeftCnt = prevProject.backLeftCnt,
                designLeftCnt = prevProject.designLeftCnt,
                techStackIds = listOf(),
                due = prevProject.due,
                myPosition = null
            ),
            userId = user.id
        )

        result.id shouldBe prevProject.id
        result.description shouldBe prevProject.description
        result.title shouldBe "Changed Title"
        result.tasks!![0].id shouldBe user.id
    }

    @DisplayName("프로젝트 수정: projectStack 데이터가 삭제되고 재생성된다")
    @Transactional
    @Test
    fun updateProjectWithTechStacksOK() {
        val user = database.getUser()
        val stacks = database.getTechStacks()
        val prevProject = projectService.createProject(
            command = DefaultCommand.CreateProject.copy(
                techStackIds = listOf(Pair(first = stacks[0].id, second = stacks[0].position))
            ),
            userId = user.id
        )
        database.flushAndClear()
        val result = projectService.updateProject(
            id = prevProject.id,
            command = CreateProject(
                title = prevProject.title,
                description = prevProject.description,
                thumbnail = prevProject.thumbnail,
                frontLeftCnt = prevProject.frontLeftCnt,
                backLeftCnt = prevProject.backLeftCnt,
                designLeftCnt = prevProject.designLeftCnt,
                techStackIds = stacks.map { Pair(first = it.id, second = it.position) },
                due = prevProject.due,
                myPosition = null
            ),
            userId = user.id
        )

        result.id shouldBe prevProject.id
        result.title shouldBe prevProject.title
        result.description shouldBe prevProject.description
        result.techStacks.map { it.name } shouldContainAll stacks.map { it.name }
        result.techStacks.map { it.aliases } shouldContainAll stacks.map { it.aliases }
        result.techStacks.map { it.position } shouldContainAll stacks.map { it.position }
        result.tasks!![0].id shouldBe user.id
    }

    @DisplayName("프로젝트 수정 : 프로젝트 외부자가 프로젝트 수정 시도시 예외가 발생한다")
    @Transactional
    @Test
    fun updateProjectTaskNotFoundException() {
        val user = database.getUser()
        val project = projectService.createProject(command = DefaultCommand.CreateProject, userId = user.id)

        shouldThrowExactly<DataNotFoundException> {
            projectService.updateProject(
                id = project.id,
                command = DefaultCommand.CreateProject.copy(title = "I have changed"),
                userId = 99999999999
            )
        }
    }

    @DisplayName("프로젝트 수정 : 리더가 아닌 멤버가 프로젝트 수정 시도시 예외가 발생한다")
    @Transactional
    @Test
    fun updateProjectNotLeaderException() {
        val users = database.getUsers()
        val project = projectService.createProject(command = DefaultCommand.CreateProject, userId = users[0].id)
        projectService.join(
            id = project.id,
            userId = users[1].id,
            position = Position.FRONTEND,
            introduction = "Hello!"
        )
        val notLeaderMember = taskRepository.findByUserIdAndProjectId(users[1].id, project.id).get()

        notLeaderMember.state shouldNotBe State.LEADER
        shouldThrowExactly<NotAllowedException> {
            projectService.updateProject(
                id = project.id,
                command = DefaultCommand.CreateProject.copy(title = "I have changed"),
                userId = users[1].id
            )
        }
    }

    @DisplayName("프로젝트 참여 : id에 해당되는 프로젝트 1개에 guest로 참여하며 task를 생성하고, 인삿말로 쓰레드를 생성한다.")
    @Transactional
    @Test
    fun joinOKCreatesTaskAndThread() {
        val leaderId = 1L
        val guestId = 2L
        val projectId = 2L
        database.getUsers()
        for (i in 0 until 3) {
            projectService.createProject(
                command = DefaultCommand.CreateProject.copy(myPosition = Position.BACKEND),
                userId = leaderId
            )
        }
        val result = projectService.join(
            id = projectId,
            userId = guestId,
            position = Position.FRONTEND,
            introduction = "Hello!"
        )
        database.flushAndClear()
        val newGuest = taskRepository.findByUserIdAndProjectId(guestId, projectId).get()
        val project = projectViewRepository.findAll().filter { it.id == projectId }
        val introThread = threadViewRepository.findById(1L).get()

        result shouldBe true
        newGuest.state shouldBe State.GUEST
        newGuest.position shouldBe Position.FRONTEND
        project[0].tasks.map { it.user.id } shouldContainAll listOf(leaderId, guestId)
        project[0].tasks.map { it.position } shouldContainAll listOf(Position.BACKEND, Position.FRONTEND)
        introThread.user.id shouldBe guestId
        introThread.content shouldBe "Hello!"
        introThread.projectId shouldBe projectId
    }

    @DisplayName("프로젝트 참여 : 복수의 프로젝트에 GUEST로 지원 중인 프로젝트에 참여 중이어도 새로운 프로젝트에 지원할 수 있다")
    @Transactional
    @Test
    fun joinMultipleProjectsAsGuestOk() {
        val users = database.getUsers()

        for (i in 0 until 2) {
            projectService.createProject(command = DefaultCommand.CreateProject, userId = users[0].id)
            projectService.createProject(command = DefaultCommand.CreateProject, userId = users[1].id)
        }

        shouldNotThrowExactly<JoinException> {
            for (i in 1L until 5L) {
                projectService.join(
                    id = i,
                    userId = users[2].id,
                    position = Position.FRONTEND,
                    introduction = "Hello!"
                )
            }
        }
    }

    @DisplayName("프로젝트 참여 : 이미 3개의 프로젝트에 Member 혹은 Leader로 참여 중인 사용자가 다른 프로젝트에 참여 시도시 예외가 발생한다")
    @Transactional
    @Test
    fun joinProjectNumberLimitException() {
        val users = database.getUsers()

        for (i in 0 until 3) {
            projectService.createProject(command = DefaultCommand.CreateProject, userId = users[0].id)
        }
        val fourthProject = projectService.createProject(command = DefaultCommand.CreateProject, userId = users[1].id)

        shouldThrowExactly<JoinException> {
            projectService.join(
                id = fourthProject.id,
                userId = users[0].id,
                position = Position.FRONTEND,
                introduction = "I wanna Be a Member!"
            )
        }
    }

    @DisplayName("프로젝트 참여 : 사용자가 이미 참여중인 프로젝트에 참여 시도시 예외가 발생한다")
    @Transactional
    @Test
    fun joinProjectAlreadyMemberException() {
        val users = database.getUsers()

        val project = projectService.createProject(command = DefaultCommand.CreateProject, userId = users[0].id)

        shouldThrowExactly<JoinException> {
            for (i in 0 until 2) {
                projectService.join(
                    id = project.id,
                    userId = users[1].id,
                    position = Position.FRONTEND,
                    introduction = "Hello!"
                )
            }
        }
    }

    @DisplayName("프로젝트 참여 : 참여하려는 프로젝트가 없으면 예외가 발생한다")
    @Transactional
    @Test
    fun joinProjectNotFoundException() {
        val user = database.getUser()

        shouldThrowExactly<DataNotFoundException> {
            projectService.join(
                id = 9999999999999999,
                userId = user.id,
                position = Position.FRONTEND,
                introduction = "Hello!"
            )
        }
    }

    @DisplayName("프로젝트 참여 : 참여하려는 프로젝트가 리크루트 모드가 아니면 예외가 발생한다")
    @Transactional
    @Test
    fun joinProjectNotRecruitingException() {
        val users = database.getUsers()

        val project = projectService.createProject(command = DefaultCommand.CreateProject, userId = users[0].id)
        projectViewRepository.save(
            projectViewRepository.getById(project.id).copy(recruiting = false)
        )

        shouldThrowExactly<JoinException> {
            projectService.join(
                id = project.id,
                userId = users[1].id,
                position = Position.FRONTEND,
                introduction = "Hello!"
            )
        }
    }

    @DisplayName("프로젝트 참여 : 프로젝트에 지원하는 포지션을 WHATEVER로 입력하면 예외가 발생한다")
    @Transactional
    @Test
    fun joinPositionNotChosenException() {
        val users = database.getUsers()

        val project = projectService.createProject(command = DefaultCommand.CreateProject, userId = users[0].id)

        shouldThrowExactly<JoinException> {
            projectService.join(
                id = project.id,
                userId = users[1].id,
                position = Position.WHATEVER,
                introduction = "Hello!"
            )
        }
    }

    @DisplayName("프로젝트 참여 : 프로젝트에 지원하는 포지션을 WHATEVER로 입력하면 예외가 발생한다")
    @Transactional
    @Test
    fun joinPositionFullException() {
        val users = database.getUsers()

        val project = projectService.createProject(
            command = DefaultCommand.CreateProject.copy(frontLeftCnt = 0),
            userId = users[0].id
        )
        shouldThrowExactly<JoinException> {
            projectService.join(
                id = project.id,
                userId = users[1].id,
                position = Position.FRONTEND,
                introduction = "Hello!"
            )
        }
    }

    @DisplayName("프로젝트 삭제 : 프로젝트 리더는 id에 해당되는 프로젝트 1개만 삭제한다.")
    @Transactional
    @Test
    fun deleteProjectOK() {
        val projectId = 2L
        val user = database.getUser()
        for (i in 0 until 3) {
            projectService.createProject(command = DefaultCommand.CreateProject, userId = user.id)
        }
        val projectLeader = taskRepository.findByUserIdAndProjectId(user.id, projectId).get()
        val dbExistingProjects = projectRepository.findAll()

        val result = projectService.deleteProject(id = projectId, userId = user.id)

        database.flushAndClear()
        val remainingProjects = projectRepository.findAll()
        val dbDeletedProject = projectRepository.findById(projectId)

        result shouldBe true
        projectLeader.state shouldBe State.LEADER
        dbExistingProjects.size shouldBe 3
        remainingProjects.size shouldBe 2
        dbDeletedProject shouldBe Optional.empty()
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

        val result = projectService.deleteProject(id = projectId, userId = user.id)

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

    @DisplayName("프로젝트 삭제 : 프로젝트 외부자가 프로젝트 삭제 시도시 예외가 발생한다")
    @Transactional
    @Test
    fun deleteProjectTaskNotFoundException() {
        val user = database.getUser()
        val project = projectService.createProject(command = DefaultCommand.CreateProject, userId = user.id)

        shouldThrowExactly<NotAllowedException> {
            projectService.deleteProject(id = project.id, userId = 99999999999)
        }
    }

    @DisplayName("프로젝트 삭제 : 프로젝트 리더가 아닌 멤버가 프로젝트 삭제 시도시 예외가 발생한다")
    @Transactional
    @Test
    fun deleteProjectNotLeaderException() {
        val users = database.getUsers()
        val project = projectService.createProject(command = DefaultCommand.CreateProject, userId = users[0].id)
        projectService.join(
            id = project.id,
            userId = users[1].id,
            position = Position.FRONTEND,
            introduction = "Hello!"
        )
        val notLeaderMember = taskRepository.findByUserIdAndProjectId(users[1].id, project.id).get()

        notLeaderMember.state shouldNotBe State.LEADER
        shouldThrowExactly<NotAllowedException> {
            projectService.deleteProject(id = project.id, userId = users[1].id)
        }
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
            due = Due.SIX,
            myPosition = Position.WHATEVER
        )
    }
}
