package waffle.guam.test

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.transaction.annotation.Transactional
import waffle.guam.Database
import waffle.guam.DatabaseTest
import waffle.guam.db.entity.Position
import waffle.guam.db.entity.TaskEntity
import waffle.guam.db.entity.UserState
import waffle.guam.db.repository.CommentRepository
import waffle.guam.db.repository.ImageRepository
import waffle.guam.db.repository.ProjectRepository
import waffle.guam.db.repository.TaskRepository
import waffle.guam.db.repository.ThreadRepository
import waffle.guam.db.repository.ThreadViewRepository
import waffle.guam.exception.DataNotFoundException
import waffle.guam.exception.NotAllowedException
import waffle.guam.service.ChatService
import waffle.guam.service.ImageService
import waffle.guam.service.command.CreateComment
import waffle.guam.service.command.CreateThread
import waffle.guam.service.command.DeleteComment
import waffle.guam.service.command.DeleteCommentImage
import waffle.guam.service.command.DeleteThread
import waffle.guam.service.command.DeleteThreadImage
import waffle.guam.service.command.EditCommentContent
import waffle.guam.service.command.EditThreadContent
import waffle.guam.service.command.RemoveNoticeThread
import waffle.guam.service.command.SetNoticeThread

@DatabaseTest
class NoticeThreadSpec @Autowired constructor(
    private val threadRepository: ThreadRepository,
    private val threadViewRepository: ThreadViewRepository,
    private val commentRepository: CommentRepository,
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository,
    private val imageRepository: ImageRepository,
    private val database: Database
) {

    val imageService = mockk<ImageService>()

    val chatService = ChatService(
        threadRepository = threadRepository,
        threadViewRepository = threadViewRepository,
        commentRepository = commentRepository,
        projectRepository = projectRepository,
        taskRepository = taskRepository,
        imageRepository = imageRepository,
        imageService = imageService,
    )

    @BeforeEach
    fun clearDatabase() {
        database.cleanUp()
    }

    @DisplayName("공지 쓰레드 설정 : projectId에 해당하는 프로젝트의 멤버는 공지 쓰레드로 threadId에 해당하는 쓰레드를 설정할 수 있다.")
    @Transactional
    @Test
    fun setNoticeThreadOK() {
        val user = database.getUser()
        val prevProject = database.getProject().copy()
        taskRepository.save(
            DefaultInput.task.copy(
                userState = UserState.MEMBER
            )
        )
        val thread = database.getThread()

        val result = chatService.setNoticeThread(
            DefaultCommand.SetNoticeThread.copy(
                projectId = prevProject.id,
                threadId = thread.id,
                userId = user.id
            )
        )
        val updatedProject = projectRepository.findById(prevProject.id).get()
        result shouldBe true
        updatedProject.id shouldBe prevProject.id
        prevProject.noticeThreadId shouldBe null
        updatedProject.noticeThreadId shouldBe thread.id
    }

    @DisplayName("공지 쓰레드 설정 : 존재하지 않는 프로젝트에 공지 쓰레드를 설정하려고 하면 예외가 발생한다")
    @Transactional
    @Test
    fun setNoticeThreadProjectNotFoundException() {
        database.getProject()
        shouldThrowExactly<DataNotFoundException> {
            chatService.setNoticeThread(
                command = DefaultCommand.SetNoticeThread.copy(projectId = 9999999)
            )
        }
    }

    @DisplayName("공지 쓰레드 설정 : 프로젝트와 아무런 연관이 없는 사용자(=task가 없는 경우)가 쓰레드를 설정하려고 하면 예외가 발생한다")
    @Transactional
    @Test
    fun setNoticeThreadTaskNotFoundException() {
        val users = database.getUsers()
        val project = database.getProject()
        taskRepository.save(
            DefaultInput.task.copy(projectId = project.id, userId = users[0].id, userState = UserState.LEADER)
        )
        shouldThrowExactly<DataNotFoundException> {
            chatService.setNoticeThread(
                command = DefaultCommand.SetNoticeThread.copy(
                    projectId = project.id,
                    userId = users[2].id
                )
            )
        }
    }

    @DisplayName("공지 쓰레드 설정 : GUEST 상태의 사용자가 쓰레드를 설정하려고 하면 예외가 발생한다")
    @Transactional
    @Test
    fun setNoticeThreadGuestTaskNotAllowedException() {
        val users = database.getUsers()
        val project = database.getProject()
        taskRepository.saveAll(
            listOf(
                DefaultInput.task.copy(projectId = project.id, userId = users[0].id, userState = UserState.LEADER),
                DefaultInput.task.copy(projectId = project.id, userId = users[1].id, userState = UserState.MEMBER),
                DefaultInput.task.copy(projectId = project.id, userId = users[2].id, userState = UserState.GUEST),
            )
        )
        shouldThrowExactly<NotAllowedException> {
            chatService.setNoticeThread(
                command = DefaultCommand.SetNoticeThread.copy(
                    projectId = project.id,
                    userId = users[2].id
                )
            )
        }
    }

    @DisplayName("공지 쓰레드 설정 : DECLINED 상태의 사용자가 쓰레드를 설정하려고 하면 예외가 발생한다")
    @Transactional
    @Test
    fun setNoticeThreadDeclinedTaskNotAllowedException() {
        val users = database.getUsers()
        val project = database.getProject()
        taskRepository.saveAll(
            listOf(
                DefaultInput.task.copy(projectId = project.id, userId = users[0].id, userState = UserState.LEADER),
                DefaultInput.task.copy(projectId = project.id, userId = users[1].id, userState = UserState.MEMBER),
                DefaultInput.task.copy(projectId = project.id, userId = users[2].id, userState = UserState.DECLINED),
            )
        )
        shouldThrowExactly<NotAllowedException> {
            chatService.setNoticeThread(
                command = DefaultCommand.SetNoticeThread.copy(
                    projectId = project.id,
                    userId = users[2].id
                )
            )
        }
    }

    // TODO(됐다가 안됐다 거림)
    @DisplayName("공지 쓰레드 설정 : threadId에 해당하는 쓰레드가 존재하지 않을 때 해당 쓰레드를 공지 쓰레드로 설정하려고 하면 예외가 발생한다")
    @Transactional
    @Test
    fun setNoticeThreadThreadNotFoundException() {
        val user = database.getUser()
        val project = database.getProject()
        taskRepository.save(DefaultInput.task)
        database.flushAndClear()
        shouldThrowExactly<DataNotFoundException> {
            chatService.setNoticeThread(
                command = DefaultCommand.SetNoticeThread.copy(
                    projectId = project.id,
                    threadId = 9999999999999999,
                    userId = user.id
                )
            )
        }
    }

    @DisplayName("공지 쓰레드 제거 : projectId에 해당하는 프로젝트의 멤버는 공지 쓰레드를 삭제할 수 있다.")
    @Transactional
    @Test
    fun removeNoticeThreadOK() {
        val user = database.getUser()
        val prevProject = database.getProject().copy()
        taskRepository.save(DefaultInput.task.copy())
        val thread = database.getThread()

        chatService.setNoticeThread(
            DefaultCommand.SetNoticeThread.copy(
                projectId = prevProject.id,
                threadId = thread.id,
                userId = user.id
            )
        )
        val prevProject2 = projectRepository.findById(prevProject.id).get().copy()
        val result = chatService.removeNoticeThread(
            DefaultCommand.RemoveNoticeThread.copy(
                projectId = prevProject2.id,
                userId = user.id
            )
        )
        val updatedProject = projectRepository.findById(prevProject2.id).get()
        result shouldBe true
        prevProject.id shouldBe prevProject2.id
        prevProject2.id shouldBe updatedProject.id
        prevProject.noticeThreadId shouldBe null
        prevProject2.noticeThreadId shouldBe thread.id
        updatedProject.noticeThreadId shouldBe null
    }

    @DisplayName("공지 쓰레드 제거 : 존재하지 않는 프로젝트에 공지 쓰레드를 삭제하려고 하면 예외가 발생한다")
    @Transactional
    @Test
    fun removeNoticeThreadProjectNotFoundException() {
        database.getProject()
        shouldThrowExactly<DataNotFoundException> {
            chatService.removeNoticeThread(
                command = DefaultCommand.RemoveNoticeThread.copy(projectId = 9999999)
            )
        }
    }

    @DisplayName("공지 쓰레드 제거 : 프로젝트와 연관이 없는 사용자(=task가 없는 경우)가 쓰레드를 삭제하려고 하면 예외가 발생한다")
    @Transactional
    @Test
    fun removeNoticeThreadTaskNotFoundException() {
        val users = database.getUsers()
        val project = database.getProject()
        taskRepository.saveAll(
            listOf(
                DefaultInput.task.copy(projectId = project.id, userId = users[0].id, userState = UserState.LEADER),
                DefaultInput.task.copy(projectId = project.id, userId = users[1].id, userState = UserState.MEMBER),
            )
        )
        shouldThrowExactly<DataNotFoundException> {
            chatService.removeNoticeThread(
                command = DefaultCommand.RemoveNoticeThread.copy(
                    projectId = project.id,
                    userId = users[2].id
                )
            )
        }
    }

    @DisplayName("공지 쓰레드 제거 : GUEST 상태의 사용자가 쓰레드를 삭제하려고 하면 예외가 발생한다")
    @Transactional
    @Test
    fun removeNoticeThreadGuestTaskNotAllowedException() {
        val users = database.getUsers()
        val project = database.getProject()
        taskRepository.saveAll(
            listOf(
                DefaultInput.task.copy(projectId = project.id, userId = users[0].id, userState = UserState.LEADER),
                DefaultInput.task.copy(projectId = project.id, userId = users[1].id, userState = UserState.MEMBER),
                DefaultInput.task.copy(projectId = project.id, userId = users[2].id, userState = UserState.GUEST),
            )
        )
        shouldThrowExactly<NotAllowedException> {
            chatService.removeNoticeThread(
                command = DefaultCommand.RemoveNoticeThread.copy(
                    projectId = project.id,
                    userId = users[2].id
                )
            )
        }
    }

    @DisplayName("공지 쓰레드 제거 : DECLINED 상태의 사용자가 쓰레드를 삭제하려고 하면 예외가 발생한다")
    @Transactional
    @Test
    fun removeNoticeThreadDeclinedTaskNotAllowedException() {
        val users = database.getUsers()
        val project = database.getProject()
        taskRepository.saveAll(
            listOf(
                DefaultInput.task.copy(projectId = project.id, userId = users[0].id, userState = UserState.LEADER),
                DefaultInput.task.copy(projectId = project.id, userId = users[1].id, userState = UserState.MEMBER),
                DefaultInput.task.copy(projectId = project.id, userId = users[2].id, userState = UserState.DECLINED),
            )
        )
        shouldThrowExactly<NotAllowedException> {
            chatService.removeNoticeThread(
                command = DefaultCommand.RemoveNoticeThread.copy(
                    projectId = project.id,
                    userId = users[2].id
                )
            )
        }
    }

    object DefaultCommand {
        val SetNoticeThread = SetNoticeThread(
            projectId = 1,
            threadId = 1,
            userId = 1
        )

        val RemoveNoticeThread = RemoveNoticeThread(
            projectId = 1,
            userId = 1
        )

        val CreateThread = CreateThread(
            projectId = 1,
            userId = 1,
            content = "New Thread",
            imageFiles = null
        )
        val EditThreadContent = EditThreadContent(
            threadId = 1,
            userId = 1,
            content = "edited Content"
        )
        val DeleteThreadImage = DeleteThreadImage(
            imageId = 1,
            threadId = 1,
            userId = 1,
        )
        val DeleteThread = DeleteThread(
            threadId = 1,
            userId = 1,
        )
        val CreateComment = CreateComment(
            threadId = 1,
            userId = 1,
            content = "New Comment",
            imageFiles = null
        )
        val EditCommentContent = EditCommentContent(
            commentId = 1,
            userId = 1,
            content = "edited Content"
        )
        val DeleteCommentImage = DeleteCommentImage(
            imageId = 1,
            commentId = 1,
            userId = 1,
        )
        val DeleteComment = DeleteComment(
            commentId = 1,
            userId = 1,
        )
    }

    object DefaultInput {
        val task = TaskEntity(
            position = Position.FRONTEND,
            projectId = 1,
            userId = 1,
            userState = UserState.MEMBER
        )

        val imageFiles = listOf(
            MockMultipartFile("파일1", "기존 파일명1.png", MediaType.IMAGE_PNG_VALUE, "파일 1 내용".toByteArray()),
            MockMultipartFile("파일2", "기존 파일명2.png", MediaType.IMAGE_PNG_VALUE, "파일 2 내용".toByteArray()),
            MockMultipartFile("파일3", "기존 파일명3.png", MediaType.IMAGE_PNG_VALUE, "파일 3 내용".toByteArray())
        )
    }
}
