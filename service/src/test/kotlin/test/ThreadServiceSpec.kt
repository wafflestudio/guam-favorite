package waffle.guam.test

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.collections.shouldNotContainAnyOf
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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
import waffle.guam.db.entity.ThreadEntity
import waffle.guam.db.entity.UserState
import waffle.guam.db.repository.CommentRepository
import waffle.guam.db.repository.ImageRepository
import waffle.guam.db.repository.ProjectRepository
import waffle.guam.db.repository.TaskRepository
import waffle.guam.db.repository.ThreadRepository
import waffle.guam.db.repository.ThreadViewRepository
import waffle.guam.exception.DataNotFoundException
import waffle.guam.exception.InvalidRequestException
import waffle.guam.exception.NotAllowedException
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
import java.util.Optional

@DatabaseTest
class ThreadServiceSpec @Autowired constructor(
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

    @DisplayName("쓰레드 생성 : projectId에 해당하는 프로젝트에 content 정보로 쓰레드를 생성한다")
    @Transactional
    @Test
    fun createThreadWithContentOK() {
        val user = database.getUser()
        val project = database.getProject()
        taskRepository.save(DefaultInput.task)
        val result = chatService.createThread(
            command = DefaultCommand.CreateThread.copy(projectId = project.id, userId = user.id)
        )
        val createdThread = threadRepository.findById(1).get()

        result shouldBe true
        createdThread.id shouldBe 1
        createdThread.projectId shouldBe project.id
        createdThread.userId shouldBe user.id
        createdThread.content shouldBe "New Thread"
    }

    @DisplayName("쓰레드 생성 : projectId에 해당하는 프로젝트에 imageFiles 배열 정보로 쓰레드를 생성한다")
    @Transactional
    @Test
    fun createThreadWithImagesOK() {
        val targetThreadId = 2L
        val user = database.getUser()
        val project = database.getProject()
        taskRepository.save(DefaultInput.task)
        for (threadId in listOf(1L, 2L, 3L)) {
            for (imageFile in DefaultInput.imageFiles) {
                every {
                    imageService.upload(imageFile, ImageInfo(threadId, ImageType.THREAD))
                } returns imageRepository.save(ImageEntity(parentId = threadId, type = ImageType.THREAD))
            }
        }

        for (i in listOf(1, 2, 3)) {
            chatService.createThread(
                command = DefaultCommand.CreateThread.copy(
                    projectId = project.id,
                    userId = user.id,
                    content = null,
                    imageFiles = DefaultInput.imageFiles
                )
            )
        }
        val createdThread = threadRepository.findById(targetThreadId).get()
        val createdImages = database.getImages()

        createdThread.id shouldBe targetThreadId
        createdThread.projectId shouldBe project.id
        createdThread.userId shouldBe user.id
        createdThread.content shouldBe null
        createdImages.size shouldBe 9
        createdImages[3].id shouldBe 4
        createdImages[3].type shouldBe ImageType.THREAD
        createdImages[3].parentId shouldBe targetThreadId
    }

    @DisplayName("쓰레드 생성 : projectId에 해당하는 프로젝트에 content와 imageFiles 배열 정보로 쓰레드를 생성한다")
    @Transactional
    @Test
    fun createThreadWithContentAndImagesOK() {
        val targetThreadId = 3L
        val user = database.getUser()
        val project = database.getProject()
        taskRepository.save(DefaultInput.task)
        for (threadId in listOf(1L, 2L, 3L)) {
            for (imageFile in DefaultInput.imageFiles) {
                every {
                    imageService.upload(imageFile, ImageInfo(threadId, ImageType.THREAD))
                } returns imageRepository.save(ImageEntity(parentId = threadId, type = ImageType.THREAD))
            }
        }
        for (i in listOf(1, 2, 3)) {
            chatService.createThread(
                command = DefaultCommand.CreateThread.copy(
                    projectId = project.id,
                    userId = user.id,
                    content = "Content for Thread $i",
                    imageFiles = DefaultInput.imageFiles
                )
            )
        }
        val createdThread = threadRepository.findById(targetThreadId).get()
        val createdImages = database.getImages()

        createdThread.id shouldBe targetThreadId
        createdThread.projectId shouldBe project.id
        createdThread.userId shouldBe user.id
        createdThread.content shouldBe "Content for Thread 3"
        createdImages.size shouldBe 9
        createdImages[6].id shouldBe 7
        createdImages[6].type shouldBe ImageType.THREAD
        createdImages[6].parentId shouldBe targetThreadId
    }

    @DisplayName("쓰레드 생성 : content로 빈 문자열 입력시 null로 간주된다")
    @Transactional
    @Test
    fun createThreadBlankInputBecomesNullOK() {
        val targetThreadId = 1L
        val user = database.getUser()
        val project = database.getProject()
        taskRepository.save(DefaultInput.task)
        every {
            imageService.upload(DefaultInput.imageFiles[0], ImageInfo(targetThreadId, ImageType.THREAD))
        } returns imageRepository.save(ImageEntity(parentId = targetThreadId, type = ImageType.THREAD))

        chatService.createThread(
            command = DefaultCommand.CreateThread.copy(
                projectId = project.id,
                userId = user.id,
                content = "             ",
                imageFiles = listOf(DefaultInput.imageFiles[0])
            )
        )

        val createdThread = threadRepository.findById(targetThreadId).get()

        createdThread.id shouldBe targetThreadId
        createdThread.content shouldBe null
    }

    @DisplayName("쓰레드 생성 : content로 입력된 문자열의 좌우 공백은 제거된다")
    @Transactional
    @Test
    fun createThreadWhiteSpaceTrimOK() {
        val targetThreadId = 1L
        val user = database.getUser()
        val project = database.getProject()
        taskRepository.save(DefaultInput.task)
        every {
            imageService.upload(DefaultInput.imageFiles[0], ImageInfo(targetThreadId, ImageType.THREAD))
        } returns imageRepository.save(ImageEntity(parentId = targetThreadId, type = ImageType.THREAD))

        chatService.createThread(
            command = DefaultCommand.CreateThread.copy(
                projectId = project.id,
                userId = user.id,
                content = "      Content With Whitespace       ",
                imageFiles = listOf(DefaultInput.imageFiles[0])
            )
        )

        val createdThread = threadRepository.findById(targetThreadId).get()

        createdThread.id shouldBe targetThreadId
        createdThread.content shouldBe "Content With Whitespace"
    }

    @DisplayName("쓰레드 생성 : 프로젝트와 관련없는 사용자(=task가 없는 경우)가 쓰레드를 생성하려고 하면 예외가 발생한다")
    @Transactional
    @Test
    fun createThreadTaskNotFoundException() {
        database.getUser()
        val project = database.getProject()
        shouldThrowExactly<DataNotFoundException> {
            chatService.createThread(
                command = DefaultCommand.CreateThread.copy(projectId = project.id, userId = 999)
            )
        }
    }

    @DisplayName("쓰레드 생성 : GUEST는 프로젝트 지원을 위한 쓰레드 1개는 생성 가능하다")
    @Transactional
    @Test
    fun createThreadGuestOneThreadOK() {
        val user = database.getUser()
        val project = database.getProject()
        taskRepository.save(DefaultInput.task.copy(userState = UserState.GUEST))
        val result = chatService.createThread(
            command = DefaultCommand.CreateThread.copy(projectId = project.id, userId = user.id)
        )
        val createdThread = threadRepository.findById(1).get()

        result shouldBe true
        createdThread.id shouldBe 1
        createdThread.projectId shouldBe project.id
        createdThread.userId shouldBe user.id
        createdThread.content shouldBe "New Thread"
    }

    @DisplayName("쓰레드 생성 : GUEST가 2번째 쓰레드를 생성하려고 시도하면 예외가 발생한다")
    @Transactional
    @Test
    fun createThreadGuestMoreThreadNotAllowedException() {
        val user = database.getUser()
        val project = database.getProject()
        taskRepository.save(DefaultInput.task.copy(userState = UserState.GUEST))
        chatService.createThread(
            command = DefaultCommand.CreateThread.copy(projectId = project.id, userId = user.id)
        )
        shouldThrowExactly<NotAllowedException> {
            chatService.createThread(
                command = DefaultCommand.CreateThread.copy(projectId = project.id, userId = user.id)
            )
        }
    }

    @DisplayName("쓰레드 생성 : 프로젝트를 그만둔 사용자(QUIT)가 쓰레드를 생성하려고 시도하면 예외가 발생한다")
    @Transactional
    @Test
    fun createThreadQuitTaskAllowedException() {
        val user = database.getUser()
        val project = database.getProject()
        taskRepository.save(DefaultInput.task.copy(userState = UserState.QUIT))
        shouldThrowExactly<NotAllowedException> {
            chatService.createThread(
                command = DefaultCommand.CreateThread.copy(projectId = project.id, userId = user.id)
            )
        }
    }

    @DisplayName("쓰레드 생성 : content와 imageFiles 배열 정보가 null이라면 예외가 발생한다")
    @Transactional
    @Test
    fun createThreadNullInputException() {
        shouldThrowExactly<InvalidRequestException> {
            chatService.createThread(
                command = DefaultCommand.CreateThread.copy(content = null, imageFiles = null)
            )
        }
    }

    @DisplayName("쓰레드 생성 : content와 imageFiles 배열 정보가 비어있다면 예외가 발생한다")
    @Transactional
    @Test
    fun createThreadEmptyNoInputException() {
        shouldThrowExactly<InvalidRequestException> {
            chatService.createThread(
                command = DefaultCommand.CreateThread.copy(content = "    ", imageFiles = listOf())
            )
        }
    }

    @DisplayName("쓰레드 생성 : projectId에 해당하는 프로젝트가 없다면 예외가 발생한다")
    @Transactional
    @Test
    fun createThreadThreadNotFoundException() {
        shouldThrowExactly<DataNotFoundException> {
            chatService.createThread(
                command = DefaultCommand.CreateThread.copy(projectId = 9999999)
            )
        }
    }

    @DisplayName("쓰레드 생성 : 이미지 이외의 파일을 업로드하려고 하면 예외가 발생한다")
    @Transactional
    @Test
    fun createThreadNonImageFileUploadException() {
        database.getUser()
        val project = database.getProject()

        every {
            imageService.upload(DefaultInput.imageFiles[0], ImageInfo(1L, ImageType.THREAD))
        } returns imageRepository.save(ImageEntity(parentId = 1L, type = ImageType.THREAD))

        shouldThrowExactly<InvalidRequestException> {
            chatService.createThread(
                command = DefaultCommand.CreateThread.copy(
                    projectId = project.id,
                    content = "Only ImageFiles Are Allowed",
                    imageFiles = listOf(
                        DefaultInput.imageFiles[0],
                        MockMultipartFile("영상 파일", "기존 파일명.mp4", "video/mp4", "영상 내용".toByteArray())
                    )
                )
            )
        }
    }

    @DisplayName("쓰레드 수정 : threadId에 해당하는 쓰레드의 작성자는 쓰레드를 수정할 수 있다.")
    @Transactional
    @Test
    fun editThreadContentOK() {
        val user = database.getUser()
        val project = database.getProject()
        taskRepository.save(DefaultInput.task)
        chatService.createThread(
            command = DefaultCommand.CreateThread.copy(projectId = project.id, userId = user.id)
        )
        val createdThread = threadRepository.findById(1).get().copy()
        Thread.sleep(10)
        val result = chatService.editThreadContent(
            command = DefaultCommand.EditThreadContent.copy(
                threadId = 1,
                userId = user.id,
            )
        )
        database.flushAndClear()
        val editedThread = threadRepository.findById(1).get()
        result shouldBe true
        createdThread.content shouldBe "New Thread"
        editedThread.content shouldBe "edited Content"
        editedThread.createdAt shouldNotBe editedThread.modifiedAt
    }

    @DisplayName("쓰레드 수정 : 이미지가 달린 쓰레드의 content를 빈 문자열로 수정시 null로 수정된다")
    @Transactional
    @Test
    fun editThreadContentBlankInputBecomesNullOK() {
        val targetThreadId = 1L
        val user = database.getUser()
        database.getProject()
        val prevThread = database.getThread().copy()
        database.getImages()

        chatService.editThreadContent(
            command = DefaultCommand.EditThreadContent.copy(
                threadId = targetThreadId,
                userId = user.id,
                content = "             ",
            )
        )
        database.flushAndClear()
        val editedThread = threadRepository.findById(targetThreadId).get()

        prevThread.id shouldBe targetThreadId
        prevThread.content shouldBe DefaultDataInfo.thread.content
        editedThread.content shouldBe null
    }

    @DisplayName("쓰레드 수정 : 쓰레드의 content 수정시 좌우 공백은 제거되어 수정된다")
    @Transactional
    @Test
    fun editThreadContentWhiteSpaceTrimOK() {
        val targetThreadId = 1L
        val user = database.getUser()
        database.getProject()
        val prevThread = database.getThread().copy()

        chatService.editThreadContent(
            command = DefaultCommand.EditThreadContent.copy(
                threadId = targetThreadId,
                userId = user.id,
                content = "      Content With Whitespace       ",
            )
        )
        database.flushAndClear()
        val editedThread = threadRepository.findById(targetThreadId).get()

        prevThread.id shouldBe targetThreadId
        prevThread.content shouldBe DefaultDataInfo.thread.content
        editedThread.content shouldBe "Content With Whitespace"
    }

    @DisplayName("쓰레드 수정 : 이미지가 없는 쓰레드의 content가 null이 되면 해당 쓰레드는 삭제된다")
    @Transactional
    @Test
    fun editThreadContentDeletesEmptyThreadOK() {
        val targetThreadId = 1L
        val user = database.getUser()
        database.getProject()
        taskRepository.save(DefaultInput.task)
        val prevThread = database.getThread()

        chatService.editThreadContent(
            command = DefaultCommand.EditThreadContent.copy(
                threadId = targetThreadId,
                userId = user.id,
                content = "               ",
            )
        )
        database.flushAndClear()
        val editedThread = threadRepository.findById(targetThreadId)

        prevThread.id shouldBe targetThreadId
        prevThread.content shouldBe DefaultDataInfo.thread.content
        editedThread shouldBe Optional.empty()
    }

    @DisplayName("쓰레드 수정 : 이미지가 없는 쓰레드의 content가 null이 되어도 댓글 존재시 해당 쓰레드는 삭제되지 않는다")
    @Transactional
    @Test
    fun editThreadContentNotDeletesEmptyThreadWithCommentsOK() {
        val targetThreadId = 1L
        val user = database.getUser()
        database.getProject()
        taskRepository.save(DefaultInput.task)
        val prevThread = database.getThread().copy()
        val prevComment = database.getComment()

        chatService.editThreadContent(
            command = DefaultCommand.EditThreadContent.copy(
                threadId = targetThreadId,
                userId = user.id,
                content = "               ",
            )
        )
        database.flushAndClear()
        val editedThread = threadRepository.findById(targetThreadId).get()
        val commentsNotEffected = commentRepository.findByThreadId(targetThreadId)[0]

        prevThread.id shouldBe targetThreadId
        prevThread.content shouldBe DefaultDataInfo.thread.content
        editedThread.content shouldBe null
        prevComment.id shouldBe commentsNotEffected.id
    }

    @DisplayName("쓰레드 수정 : threadId에 해당하는 쓰레드가 없다면 예외가 발생한다.")
    @Transactional
    @Test
    fun editThreadThreadIdNotFoundException() {
        shouldThrowExactly<DataNotFoundException> {
            chatService.editThreadContent(
                command = DefaultCommand.EditThreadContent.copy(
                    threadId = 999999,
                )
            )
        }
    }

    @DisplayName("쓰레드 수정 : 수정하려는 쓰레드의 작성자가 아니면 예외가 발생한다.")
    @Transactional
    @Test
    fun editThreadNotCreatorException() {
        val user = database.getUser()
        val project = database.getProject()
        taskRepository.save(DefaultInput.task)
        chatService.createThread(
            command = DefaultCommand.CreateThread.copy(projectId = project.id, userId = user.id)
        )
        shouldThrowExactly<NotAllowedException> {
            chatService.editThreadContent(
                command = DefaultCommand.EditThreadContent.copy(
                    userId = 999999,
                )
            )
        }
    }

    @DisplayName("쓰레드 수정 : 이전과 동일한 content로 수정하려고 하면 예외가 발생한다.")
    @Transactional
    @Test
    fun editThreadSameContentException() {
        val user = database.getUser()
        val project = database.getProject()
        taskRepository.save(DefaultInput.task)
        chatService.createThread(
            command = DefaultCommand.CreateThread.copy(
                projectId = project.id,
                userId = user.id,
                content = "The Same Content"
            )
        )
        shouldThrowExactly<InvalidRequestException> {
            chatService.editThreadContent(
                command = DefaultCommand.EditThreadContent.copy(
                    content = "The Same Content"
                )
            )
        }
    }

    @DisplayName("쓰레드 이미지 삭제 : threadId에 해당하는 쓰레드의 작성자는 imageId에 해당하는 이미지를 쓰레드에서 삭제할 수 있다.")
    @Transactional
    @Test
    fun deleteThreadImageOK() {
        val user = database.getUser()
        database.getProject()
        val thread = database.getThread()
        val prevDBImages = database.getImages()
        val prevThreadImages = imageRepository.findByParentIdAndType(thread.id, ImageType.THREAD)
        chatService.deleteThreadImage(
            command = DefaultCommand.DeleteThreadImage.copy(
                imageId = prevThreadImages[0].id,
                threadId = thread.id,
                userId = user.id
            )
        )
        database.flushAndClear()
        val remainingThread = threadRepository.findById(thread.id).get()
        val remainingDBImages = imageRepository.findAll()
        val remainingThreadImages = imageRepository.findByParentIdAndType(thread.id, ImageType.THREAD)

        remainingThread.id shouldBe thread.id
        remainingDBImages.size shouldBe prevDBImages.size - 1
        remainingThreadImages.size shouldBe prevThreadImages.size - 1
        for (image in remainingThreadImages) {
            image.id shouldNotBe prevThreadImages[0].id
        }
    }

    @DisplayName("쓰레드 이미지 삭제 : content가 없는 쓰레드의 마지막 image 삭제시 쓰레드 자체가 삭제된다.")
    @Transactional
    @Test
    fun deleteThreadImageDeletesEmptyThreadOK() {
        val user = database.getUser()
        val project = database.getProject()
        taskRepository.save(DefaultInput.task)
        val thread = threadRepository.save(
            ThreadEntity(
                projectId = project.id,
                userId = user.id,
                content = null,
            )
        )
        val prevDBImages = database.getImages()
        val prevThreadImages = imageRepository.findByParentIdAndType(thread.id, ImageType.THREAD)
        for (threadImage in prevThreadImages) {
            chatService.deleteThreadImage(
                command = DefaultCommand.DeleteThreadImage.copy(
                    imageId = threadImage.id,
                    threadId = thread.id,
                    userId = user.id
                )
            )
        }
        database.flushAndClear()
        val deletedThread = threadRepository.findById(thread.id)
        val remainingDBImages = imageRepository.findAll()
        val remainingThreadImages = imageRepository.findByParentIdAndType(thread.id, ImageType.THREAD)

        deletedThread shouldBe Optional.empty()
        remainingDBImages.size shouldBe prevDBImages.size - 3
        prevThreadImages.size shouldBe 3
        remainingThreadImages.size shouldBe 0
        remainingThreadImages.map { it.id } shouldNotContainAnyOf prevThreadImages.map { it.id }
    }

    @DisplayName("쓰레드 이미지 삭제 : content가 없는 쓰레드의 마지막 image 삭제시 댓글이 있으면 쓰레드 자체는 삭제되지 않는다.")
    @Transactional
    @Test
    fun deleteThreadImageNotDeletesEmptyThreadWithCommentsOK() {
        val user = database.getUser()
        val project = database.getProject()
        taskRepository.save(DefaultInput.task)
        val thread = threadRepository.save(
            ThreadEntity(
                projectId = project.id,
                userId = user.id,
                content = null,
            )
        )
        database.getComment()
        val prevDBImages = database.getImages()
        val prevThreadImages = imageRepository.findByParentIdAndType(thread.id, ImageType.THREAD)
        for (threadImage in prevThreadImages) {
            chatService.deleteThreadImage(
                command = DefaultCommand.DeleteThreadImage.copy(
                    imageId = threadImage.id,
                    threadId = thread.id,
                    userId = user.id
                )
            )
        }
        database.flushAndClear()
        val deletedThread = threadRepository.findById(thread.id).get()
        val remainingDBImages = imageRepository.findAll()
        val remainingThreadImages = imageRepository.findByParentIdAndType(thread.id, ImageType.THREAD)

        deletedThread.id shouldBe thread.id
        remainingDBImages.size shouldBe prevDBImages.size - 3
        prevThreadImages.size shouldBe 3
        remainingThreadImages.size shouldBe 0
        remainingThreadImages.map { it.id } shouldNotContainAnyOf prevThreadImages.map { it.id }
    }

    @DisplayName("쓰레드 이미지 삭제 : imageId에 해당하는 이미지가 없다면 예외가 발생한다.")
    @Transactional
    @Test
    fun deleteThreadImageImageNotFoundException() {
        database.getImages()
        shouldThrowExactly<DataNotFoundException> {
            chatService.deleteThreadImage(
                command = DefaultCommand.DeleteThreadImage.copy(
                    imageId = 9999999999,
                )
            )
        }
    }

    @DisplayName("쓰레드 이미지 삭제 : threadId에 해당하는 쓰레드가 없다면 예외가 발생한다.")
    @Transactional
    @Test
    fun deleteThreadImageThreadNotFoundException() {
        val images = database.getImages()
        shouldThrowExactly<DataNotFoundException> {
            chatService.deleteThreadImage(
                command = DefaultCommand.DeleteThreadImage.copy(
                    threadId = 999999999,
                    imageId = images[0].id,
                )
            )
        }
    }

    @DisplayName("쓰레드 이미지 삭제 : 해당 쓰레드의 작성자가 아니라면 예외가 발생한다.")
    @Transactional
    @Test
    fun deleteThreadImageNotCreatorException() {
        database.getUser()
        database.getProject()
        database.getThread()
        database.getImages()

        shouldThrowExactly<NotAllowedException> {
            chatService.deleteThreadImage(
                command = DefaultCommand.DeleteThreadImage.copy(
                    userId = 999999999999999,
                )
            )
        }
    }

    @DisplayName("쓰레드 이미지 삭제 : threadId와 이미지의 parentId가 불일치하면 예외가 발생한다.")
    @Transactional
    @Test
    fun deleteThreadImageInvalidThreadIdException() {
        database.getUser()
        database.getProject()
        database.getThread()
        val image = imageRepository.save(ImageEntity(type = ImageType.THREAD, parentId = 999999999))

        shouldThrowExactly<InvalidRequestException> {
            chatService.deleteThreadImage(
                command = DefaultCommand.DeleteThreadImage.copy(
                    imageId = image.id
                )
            )
        }
    }

    // 쓰레드 이미지도 함께 제거되는가

    @DisplayName("쓰레드 삭제 : 쓰레드의 작성자는 특정 쓰레드와 관련된 이미지들을 삭제할 수 있다.")
    @Transactional
    @Test
    fun deleteThreadOK() {
        val user = database.getUser()
        val project = database.getProject()
        taskRepository.save(DefaultInput.task)
        taskRepository.save(DefaultInput.task)
        database.getImages()
        chatService.createThread(
            command = DefaultCommand.CreateThread.copy(projectId = project.id, userId = user.id)
        )
        val prevThreadImages = imageRepository.findByParentIdAndType(parentId = 1, type = ImageType.THREAD)
        val result = chatService.deleteThread(command = DefaultCommand.DeleteThread.copy(threadId = 1))
        val deletedThread = threadRepository.findById(1)
        val deletedThreadImages = imageRepository.findByParentIdAndType(parentId = 1, type = ImageType.THREAD)

        result shouldBe true
        prevThreadImages.size shouldBe 3
        deletedThreadImages.size shouldBe 0
        deletedThread shouldBe Optional.empty()
    }

    @DisplayName("쓰레드 삭제 : 댓글이 달린 쓰레드는 삭제되지 않고 content에 null 할당 & 이미지들만 삭제된다.")
    @Transactional
    @Test
    fun deleteThreadWithCommentsOK() {
        val user = database.getUser()
        val project = database.getProject()
        taskRepository.save(DefaultInput.task)
        database.getImages()
        chatService.createThread(command = DefaultCommand.CreateThread.copy(projectId = project.id, userId = user.id))
        for (i in 0 until 3) {
            chatService.createComment(
                command = DefaultCommand.CreateComment.copy(threadId = 1, userId = 1, content = "Should Not be Deleted")
            )
        }
        val prevThreadImages = imageRepository.findByParentIdAndType(parentId = 1, type = ImageType.THREAD)
        val result = chatService.deleteThread(command = DefaultCommand.DeleteThread.copy(threadId = 1))
        val deletedThread = threadRepository.findById(1).get()
        val deletedThreadImages = imageRepository.findByParentIdAndType(parentId = 1, type = ImageType.THREAD)
        val remainingCommentsInDeletedThread = commentRepository.countByThreadId(1)

        result shouldBe true
        deletedThread.content shouldBe null
        prevThreadImages.size shouldBe 3
        deletedThreadImages.size shouldBe 0
        remainingCommentsInDeletedThread shouldBe 3
    }

    @DisplayName("쓰레드 삭제 : threadId에 해당하는 쓰레드가 없다면 예외가 발생한다.")
    @Transactional
    @Test
    fun deleteThreadThreadNotFoundException() {
        shouldThrowExactly<DataNotFoundException> {
            chatService.deleteThread(
                command = DefaultCommand.DeleteThread.copy(
                    threadId = 9999999999,
                )
            )
        }
    }

    @DisplayName("쓰레드 삭제 : 삭제하려는 쓰레드의 작성자가 아니면 예외가 발생한다.")
    @Transactional
    @Test
    fun deleteThreadNotCreatorException() {
        val user = database.getUser()
        val project = database.getProject()
        taskRepository.save(DefaultInput.task)
        chatService.createThread(
            command = DefaultCommand.CreateThread.copy(projectId = project.id, userId = user.id)
        )
        shouldThrowExactly<NotAllowedException> {
            chatService.deleteThread(
                command = DefaultCommand.DeleteThread.copy(
                    userId = 999999999999999,
                )
            )
        }
    }

    @DisplayName("쓰레드 삭제 : 프로젝트와 관련없는 사용자(=task가 없는 경우)가 쓰레드를 삭제하려고 하면 예외가 발생한다")
    @Transactional
    @Test
    fun deleteThreadTaskNotFoundException() {
        val user = database.getUser()
        val project = database.getProject()
        val creatorTask = taskRepository.save(DefaultInput.task)
        chatService.createThread(
            command = DefaultCommand.CreateThread.copy(projectId = project.id, userId = user.id)
        )
        taskRepository.delete(creatorTask)
        shouldThrowExactly<DataNotFoundException> {
            chatService.deleteThread(
                command = DefaultCommand.DeleteThread.copy(
                    userId = user.id,
                )
            )
        }
    }

    @DisplayName("쓰레드 삭제 : GUEST가 자신이 작성할 쓰레드를 삭제하려고 하면 예외가 발생한다.")
    @Transactional
    @Test
    fun deleteThreadGuestNotAllowedException() {
        val user = database.getUser()
        val project = database.getProject()
        taskRepository.save(
            DefaultInput.task.copy(
                userState = UserState.GUEST
            )
        )
        chatService.createThread(
            command = DefaultCommand.CreateThread.copy(projectId = project.id, userId = user.id)
        )
        shouldThrowExactly<NotAllowedException> {
            chatService.deleteThread(
                command = DefaultCommand.DeleteThread.copy(
                    userId = user.id,
                )
            )
        }
    }

    @DisplayName("쓰레드 삭제 : 프로젝트를 그만둔 사용자(QUIT)이 자신이 작성했던 쓰레드를 삭제하려고 하면 예외가 발생한다.")
    @Transactional
    @Test
    fun deleteThreadQuitNotAllowedException() {
        val user = database.getUser()
        val project = database.getProject()
        val creatorTask = taskRepository.save(
            DefaultInput.task.copy(
                userState = UserState.MEMBER
            )
        )
        chatService.createThread(
            command = DefaultCommand.CreateThread.copy(projectId = project.id, userId = user.id)
        )
        taskRepository.save(
            creatorTask.copy(
                userState = UserState.QUIT
            )
        )
        database.flushAndClear()
        shouldThrowExactly<NotAllowedException> {
            chatService.deleteThread(
                command = DefaultCommand.DeleteThread.copy(
                    userId = user.id,
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
