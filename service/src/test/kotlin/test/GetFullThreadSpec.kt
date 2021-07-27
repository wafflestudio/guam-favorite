package waffle.guam.test

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
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
import waffle.guam.DefaultDataInfo
import waffle.guam.db.entity.ImageEntity
import waffle.guam.db.entity.ImageType
import waffle.guam.db.entity.Position
import waffle.guam.db.entity.TaskEntity
import waffle.guam.db.entity.UserState
import waffle.guam.db.repository.CommentRepository
import waffle.guam.db.repository.ImageRepository
import waffle.guam.db.repository.ProjectRepository
import waffle.guam.db.repository.TaskRepository
import waffle.guam.db.repository.ThreadRepository
import waffle.guam.db.repository.ThreadViewRepository
import waffle.guam.model.Image
import waffle.guam.model.ThreadDetail
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
class GetFullThreadSpec @Autowired constructor(
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

    // TODO("됐다 안됐다 거림")
    @DisplayName("쓰레드 상세 조회 : threadId에 해당하는 쓰레드를 찾아준다")
    @Transactional
    @Test
    fun getFullThreadOK() {
        val threadId = 3L
        val users = database.getUsers()
        val project = database.getProject()
        for (i in 1 until 5) {
            threadRepository.save(
                DefaultDataInfo.thread.copy(projectId = project.id, userId = users[0].id, content = "Thread Number $i")
            )
        }
        commentRepository.save(DefaultDataInfo.comment.copy(threadId = threadId, content = "First", userId = users[1].id))
        commentRepository.save(DefaultDataInfo.comment.copy(threadId = threadId, content = "Second", userId = users[2].id))
        database.flushAndClear()
        val result: ThreadDetail = chatService.getFullThread(threadId)
        result.id shouldBe threadId
        result.creatorId shouldBe users[0].id
        result.creatorImageUrl shouldBe users[0].image?.getPath()
        result.creatorNickname shouldBe users[0].nickname
        result.content shouldBe "Thread Number $threadId"
        result.comments[0].content shouldBe "First"
        result.comments[0].creatorNickname shouldBe users[1].nickname
        result.comments[1].content shouldBe "Second"
        result.comments[1].creatorNickname shouldBe users[2].nickname
    }

    // TODO("됐다 안됐다 거림")
    @DisplayName("쓰레드 상세 조회 : threadId에 해당하는 쓰레드와 관련 댓글들의 세부 정보를 조회할 수 있다")
    @Transactional
    @Test
    fun getFullThreadEveryDetailOK() {
        val targetThreadId = 1L
        val users = database.getUsers()
        val project = database.getProject()
        threadRepository.save(
            DefaultDataInfo.thread.copy(projectId = project.id, userId = users[0].id, content = "Thread Number 1")
        )
        database.flush()
        for (i in 1 until 6) {
            commentRepository.save(
                DefaultDataInfo.comment.copy(
                    threadId = targetThreadId, content = "Comment Number $i", userId = users[i % 3].id
                )
            )
        }
        database.flush()
        imageRepository.saveAll(
            listOf(
                ImageEntity(parentId = 1, type = ImageType.THREAD),
                ImageEntity(parentId = 1, type = ImageType.COMMENT),
                ImageEntity(parentId = 1, type = ImageType.COMMENT),
                ImageEntity(parentId = 1, type = ImageType.COMMENT),
                ImageEntity(parentId = 5, type = ImageType.COMMENT),
                ImageEntity(parentId = 5, type = ImageType.COMMENT),
            )
        )
        database.flushAndClear()
        val commentToBeEdited = commentRepository.findById(2)
        commentRepository.save(commentToBeEdited.get().copy(content = "Comment Number 2 that has been edited", modifiedAt = LocalDateTime.now()))
        database.flushAndClear()
        val threadImages = imageRepository.findByParentIdAndType(targetThreadId, ImageType.THREAD)
        val commentImages = imageRepository.findByParentIdAndType(1, ImageType.COMMENT)
        val emptyImageList = imageRepository.findByParentIdAndType(2, ImageType.COMMENT).map { Image.of(it) }

        val result: ThreadDetail = chatService.getFullThread(targetThreadId)

        result.id shouldBe targetThreadId
        result.content shouldBe "Thread Number 1"
        result.isEdited shouldBe false
        result.creatorId shouldBe users[0].id
        result.creatorImageUrl shouldBe users[0].image?.getPath()
        result.creatorNickname shouldBe users[0].nickname
        result.threadImages.size shouldBe threadImages.size
        result.threadImages[0].id shouldBe threadImages[0].id
        result.threadImages[0].path shouldBe threadImages[0].getPath()

        result.comments[0].id shouldBe 1
        result.comments[0].content shouldBe "Comment Number 1"
        result.comments[0].isEdited shouldBe false
        result.comments[0].creatorId shouldBe users[1 % 3].id
        result.comments[0].creatorNickname shouldBe users[1 % 3].nickname
        result.comments[0].creatorImageUrl shouldBe users[1 % 3].image?.getPath()
        result.comments[0].commentImages.size shouldBe commentImages.size
        result.comments[0].commentImages[0].id shouldBe commentImages[0].id
        result.comments[0].commentImages[0].path shouldBe commentImages[0].getPath()

        result.comments[1].id shouldBe 2
        result.comments[1].content shouldBe "Comment Number 2 that has been edited"
        result.comments[1].isEdited shouldBe true
        result.comments[1].creatorNickname shouldBe users[2 % 3].nickname
        result.comments[1].creatorImageUrl shouldBe users[2 % 3].image?.getPath()
        result.comments[1].commentImages.size shouldBe emptyImageList.size
        result.comments[1].commentImages shouldBe emptyImageList
    }

    @DisplayName("쓰레드 상세 조회 : content 혹은 imageFiles만 존재하는 쓰레드와 댓글들을 조회해도 예외가 발생하지 않는다.")
    @Transactional
    @Test
    fun getFullThreadNoImageOrContentOK() {
        database.getUsers()
        val project = database.getProject()
        taskRepository.save(DefaultInput.task)
        chatService.createThread(command = DefaultCommand.CreateThread.copy(content = "Only Content Thread"))
        chatService.createComment(command = DefaultCommand.CreateComment.copy(threadId = 1, content = "Only Content Comment"))

        every {
            imageService.upload(DefaultInput.imageFiles[0], ImageInfo(2L, ImageType.THREAD))
        } returns imageRepository.save(ImageEntity(parentId = 2L, type = ImageType.THREAD))
        every {
            imageService.upload(DefaultInput.imageFiles[1], ImageInfo(2L, ImageType.COMMENT))
        } returns imageRepository.save(ImageEntity(parentId = 2L, type = ImageType.COMMENT))

        chatService.createThread(
            command = DefaultCommand.CreateThread.copy(
                content = null,
                imageFiles = listOf(DefaultInput.imageFiles[0])
            )
        )
        chatService.createComment(
            command = DefaultCommand.CreateComment.copy(
                threadId = 2,
                content = null,
                imageFiles = listOf(DefaultInput.imageFiles[1])
            )
        )

        database.flushAndClear()

        val noImageResult: ThreadDetail = chatService.getFullThread(1)
        val noContentResult: ThreadDetail = chatService.getFullThread(2)

        noImageResult.id shouldBe 1
        noImageResult.content shouldBe "Only Content Thread"
        noImageResult.threadImages.size shouldBe 0
        noImageResult.comments[0].content shouldBe "Only Content Comment"
        noImageResult.comments[0].commentImages.size shouldBe 0

        noContentResult.id shouldBe 2
        noContentResult.content shouldBe null
        noContentResult.threadImages.size shouldBe 1
        noContentResult.comments[0].content shouldBe null
        noContentResult.comments[0].commentImages.size shouldBe 1
    }

    @DisplayName("쓰레드 상세 조회 : threadId에 해당하는 쓰레드가 없다면 예외가 발생한다")
    @Transactional
    @Test
    fun getFullThreadThreadNotFoundException() {
        shouldThrowExactly<RuntimeException> {
            chatService.getFullThread(99999999999)
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
