package waffle.guam.test

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.transaction.annotation.Transactional
import waffle.guam.Database
import waffle.guam.DatabaseTest
import waffle.guam.DefaultDataInfo
import waffle.guam.db.entity.ImageEntity
import waffle.guam.db.entity.ImageType
import waffle.guam.db.entity.Position
import waffle.guam.db.entity.TaskEntity
import waffle.guam.db.entity.ThreadEntity
import waffle.guam.db.entity.UserState
import waffle.guam.db.repository.CommentRepository
import waffle.guam.db.repository.ImageRepository
import waffle.guam.db.repository.ProjectRepository
import waffle.guam.db.repository.TaskRepository
import waffle.guam.db.repository.ThreadRepository
import waffle.guam.db.repository.ThreadViewRepository
import waffle.guam.model.Image
import waffle.guam.model.ThreadOverView
import waffle.guam.service.ChatService
import waffle.guam.service.ImageInfo
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
import java.time.LocalDateTime

@DatabaseTest
class GetThreadsSpec @Autowired constructor(
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

    @DisplayName("복수의 쓰레드 조회 : projectId에 해당하는 프로젝트에 달린 쓰레드들을 조회할 수 있다")
    @Transactional
    @Test
    fun getThreadsOK() {
        database.getUser()
        val project = database.getProject()
        taskRepository.save(DefaultInput.task)
        for (i in 0 until 15) {
            chatService.createThread(
                command = DefaultCommand.CreateThread.copy(content = "Thread Number $i")
            )
        }
        for (i in 0 until 5) {
            chatService.createComment(
                command = DefaultCommand.CreateComment.copy(threadId = 1, content = "Comment Number $i")
            )
            chatService.createComment(
                command = DefaultCommand.CreateComment.copy(threadId = 2, content = "Comment Number $i")
            )
        }
        val result: Page<ThreadOverView> = chatService.getThreads(
            projectId = project.id,
            pageable = PageRequest.of(
                0,
                10,
            )
        )
        result.content.size shouldBe 10
        result.content[0].content shouldBe "Thread Number 0"
        result.content[0].commentSize shouldBe 5
        result.content[5].content shouldBe "Thread Number 5"
        result.content[5].commentSize shouldBe 0
        result.pageable.offset shouldBe 0
        result.totalElements shouldBe 15
    }

    @DisplayName("복수의 쓰레드 조회 : projectId, page, size 정보에 해당되는 쓰레드들의 세부 정보를 조회할 수 있다")
    @Transactional
    @Test
    fun getThreadsEveryDetailOK() {
        val users = database.getUsers()
        val project = database.getProject()
        for (i in 1 until 5) {
            threadRepository.save(
                ThreadEntity(userId = users[i % 3].id, content = "Thread Number $i", projectId = project.id)
            )
        }
        val threadToBeEdited = threadRepository.save(
            ThreadEntity(userId = users[5 % 3].id, content = "To be edited", projectId = project.id)
        )
        for (i in 0 until 2) {
            commentRepository.save(DefaultDataInfo.comment.copy(userId = users[i % 3].id, threadId = 4, content = "filterImages test"))
        }
        imageRepository.saveAll(
            listOf(
                ImageEntity(parentId = 5, type = ImageType.THREAD),
                ImageEntity(parentId = 5, type = ImageType.COMMENT),
            )
        )
        threadRepository.save(threadToBeEdited.copy(content = "Thread Number 5 that has been edited", modifiedAt = LocalDateTime.now()))
        database.flushAndClear()
        val result: Page<ThreadOverView> = chatService.getThreads(
            projectId = project.id,
            pageable = PageRequest.of(
                1,
                3,
            )
        )
        val emptyImageList = imageRepository.findByParentIdAndType(4, ImageType.THREAD).map { Image.of(it) }
        val threadImages = imageRepository.findByParentIdAndType(5, ImageType.THREAD)

        result.content[0].id shouldBe 4
        result.content[0].content shouldBe "Thread Number 4"
        result.content[0].isEdited shouldBe false
        result.content[0].commentSize shouldBe 2
        result.content[0].creatorId shouldBe users[4 % 3].id
        result.content[0].creatorImageUrl shouldBe users[4 % 3].image?.getPath()
        result.content[0].threadImages.size shouldBe emptyImageList.size
        result.content[0].threadImages shouldBe emptyImageList

        result.content[1].id shouldBe 5
        result.content[1].content shouldBe "Thread Number 5 that has been edited"
        result.content[1].commentSize shouldBe 0
        result.content[1].isEdited shouldBe true
        result.content[1].creatorId shouldBe users[5 % 3].id
        result.content[1].creatorImageUrl shouldBe users[5 % 3].image?.getPath()
        result.content[1].threadImages.size shouldBe threadImages.size
        result.content[1].threadImages[0].id shouldBe threadImages[0].id
        result.content[1].threadImages[0].path shouldBe threadImages[0].getPath()

        result.content.size shouldBe 2
        result.pageable.offset shouldBe 3
        result.totalElements shouldBe 5
    }

    @DisplayName("복수의 쓰레드 조회 : content 혹은 imageFiles만 존재하는 쓰레드들을 조회해도 예외가 발생하지 않는다.")
    @Transactional
    @Test
    fun getThreadsNoImageOrContentOK() {
        database.getUsers()
        val project = database.getProject()
        taskRepository.save(DefaultInput.task)
        chatService.createThread(command = DefaultCommand.CreateThread.copy(content = "Only Content Thread"))

        every {
            imageService.upload(DefaultInput.imageFiles[0], ImageInfo(2L, ImageType.THREAD))
        } returns imageRepository.save(ImageEntity(parentId = 2L, type = ImageType.THREAD))

        chatService.createThread(
            command = DefaultCommand.CreateThread.copy(
                content = null,
                imageFiles = listOf(DefaultInput.imageFiles[0])
            )
        )

        val result: Page<ThreadOverView> = chatService.getThreads(
            projectId = project.id,
            pageable = PageRequest.of(
                0,
                10,
            )
        )
        database.flushAndClear()

        result.content[0].id shouldBe 1
        result.content[0].content shouldBe "Only Content Thread"
        result.content[0].threadImages.size shouldBe 0

        result.content[1].id shouldBe 2
        result.content[1].content shouldBe null
        result.content[1].threadImages.size shouldBe 1
    }

    @DisplayName("복수의 쓰레드 조회 : page와 size에 해당되는 범위에 쓰레드가 없어도 예외는 발생하지 않는다")
    @Transactional
    @Test
    fun getThreadsOutOfRangeOK() {
        val totalThreadNum = 5
        val page = 100
        val size = 100
        database.getUsers()
        val project = database.getProject()
        taskRepository.save(DefaultInput.task)
        for (i in 0 until totalThreadNum) {
            chatService.createThread(command = DefaultCommand.CreateThread)
        }
        val result = chatService.getThreads(
            projectId = project.id,
            pageable = PageRequest.of(
                page,
                size,
            )
        )
        result.content.size shouldBe 0
        result.content shouldBe listOf()
        result.pageable.offset shouldBe page * size
        result.totalElements shouldBe totalThreadNum
    }

    @DisplayName("복수의 쓰레드 조회 : projectId에 해당하는 프로젝트가 없어도 예외는 발생하지 않는다")
    @Transactional
    @Test
    fun getThreadsProjectNotFoundOK() {
        database.getUsers()
        database.getProject()
        database.getThread()
        val totalThreadNum = 0
        val page = 0
        val size = 10
        val result = chatService.getThreads(
            projectId = 999999,
            pageable = PageRequest.of(page, size)
        )
        result.content.size shouldBe 0
        result.content shouldBe listOf()
        result.pageable.offset shouldBe page * size
        result.totalElements shouldBe totalThreadNum
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
