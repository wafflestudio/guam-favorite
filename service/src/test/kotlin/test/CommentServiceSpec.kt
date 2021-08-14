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
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.transaction.annotation.Transactional
import waffle.guam.Database
import waffle.guam.DatabaseTest
import waffle.guam.DefaultDataInfo
import waffle.guam.db.entity.CommentEntity
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
class CommentServiceSpec @Autowired constructor(
    private val threadRepository: ThreadRepository,
    private val threadViewRepository: ThreadViewRepository,
    private val commentRepository: CommentRepository,
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository,
    private val imageRepository: ImageRepository,
    private val database: Database,
    private val eventPublisher: ApplicationEventPublisher
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
        eventPublisher = eventPublisher
    )

    @BeforeEach
    fun clearDatabase() {
        database.cleanUp()
    }

    @DisplayName("댓글 생성 : threadId에 해당하는 쓰레드에 content 정보로 댓글을 생성한다.")
    @Transactional
    @Test
    fun createCommentWithContentOK() {
        val user = database.getUser()
        taskRepository.save(DefaultInput.task)
        val thread = database.getThread()
        val result = chatService.createComment(
            command = DefaultCommand.CreateComment.copy(threadId = thread.id, userId = user.id)
        )
        val createdComment = commentRepository.findById(1).get()

        result shouldBe true
        createdComment.id shouldBe 1
        createdComment.threadId shouldBe thread.id
        createdComment.userId shouldBe user.id
        createdComment.content shouldBe "New Comment"
    }

    @DisplayName("댓글 생성 : projectId에 해당하는 프로젝트에 imageFiles 배열 정보로 댓글을 생성한다")
    @Transactional
    @Test
    fun createCommentWithImagesOK() {
        val targetCommentId = 2L
        val user = database.getUser()
        database.getProject()
        taskRepository.save(DefaultInput.task)
        val thread = database.getThread()

        for (commentId in listOf(1L, 2L, 3L)) {
            for (imageFile in DefaultInput.imageFiles) {
                every {
                    imageService.upload(imageFile, ImageInfo(commentId, ImageType.COMMENT))
                } returns imageRepository.save(ImageEntity(parentId = commentId, type = ImageType.COMMENT))
            }
        }

        for (i in listOf(1, 2, 3)) {
            chatService.createComment(
                command = DefaultCommand.CreateComment.copy(
                    threadId = thread.id,
                    userId = user.id,
                    content = null,
                    imageFiles = DefaultInput.imageFiles
                )
            )
        }
        val createdComment = commentRepository.findById(targetCommentId).get()
        val createdImages = database.getImages()

        createdComment.id shouldBe targetCommentId
        createdComment.threadId shouldBe thread.id
        createdComment.userId shouldBe user.id
        createdComment.content shouldBe null
        createdImages.size shouldBe 9
        createdImages[3].id shouldBe 4
        createdImages[3].type shouldBe ImageType.COMMENT
        createdImages[3].parentId shouldBe targetCommentId
    }

    @DisplayName("댓글 생성 : projectId에 해당하는 프로젝트에 content와 imageFiles 배열 정보로 댓글을 생성한다")
    @Transactional
    @Test
    fun createCommentWithContentAndImagesOK() {
        val targetCommentId = 3L
        val user = database.getUser()
        database.getProject()
        taskRepository.save(DefaultInput.task)
        val thread = database.getThread()

        for (commentId in listOf(1L, 2L, 3L)) {
            for (imageFile in DefaultInput.imageFiles) {
                every {
                    imageService.upload(imageFile, ImageInfo(commentId, ImageType.COMMENT))
                } returns imageRepository.save(ImageEntity(parentId = commentId, type = ImageType.COMMENT))
            }
        }

        for (i in listOf(1, 2, 3)) {
            chatService.createComment(
                command = DefaultCommand.CreateComment.copy(
                    threadId = thread.id,
                    userId = user.id,
                    content = "Content for Comment $i",
                    imageFiles = DefaultInput.imageFiles
                )
            )
        }
        val createdComment = commentRepository.findById(targetCommentId).get()
        val createdImages = database.getImages()

        createdComment.id shouldBe targetCommentId
        createdComment.threadId shouldBe thread.id
        createdComment.userId shouldBe user.id
        createdComment.content shouldBe "Content for Comment 3"
        createdImages.size shouldBe 9
        createdImages[6].id shouldBe 7
        createdImages[6].type shouldBe ImageType.COMMENT
        createdImages[6].parentId shouldBe targetCommentId
    }

    @DisplayName("댓글 생성 : content로 빈 문자열 입력시 null로 간주된다")
    @Transactional
    @Test
    fun createCommentBlankInputBecomesNullOK() {
        val targetCommentId = 1L
        val user = database.getUser()
        val thread = database.getThread()
        taskRepository.save(DefaultInput.task)
        every {
            imageService.upload(DefaultInput.imageFiles[0], ImageInfo(targetCommentId, ImageType.COMMENT))
        } returns imageRepository.save(ImageEntity(parentId = targetCommentId, type = ImageType.COMMENT))

        chatService.createComment(
            command = DefaultCommand.CreateComment.copy(
                threadId = thread.id,
                userId = user.id,
                content = "             ",
                imageFiles = listOf(DefaultInput.imageFiles[0])
            )
        )

        val createdComment = commentRepository.findById(targetCommentId).get()

        createdComment.id shouldBe targetCommentId
        createdComment.content shouldBe null
    }

    @DisplayName("댓글 생성 : content로 입력된 문자열의 좌우 공백은 제거된다")
    @Transactional
    @Test
    fun createCommentWhiteSpaceTrimOK() {
        val targetCommentId = 1L
        val user = database.getUser()
        val thread = database.getThread()
        taskRepository.save(DefaultInput.task)
        every {
            imageService.upload(DefaultInput.imageFiles[0], ImageInfo(targetCommentId, ImageType.COMMENT))
        } returns imageRepository.save(ImageEntity(parentId = targetCommentId, type = ImageType.COMMENT))

        chatService.createComment(
            command = DefaultCommand.CreateComment.copy(
                threadId = thread.id,
                userId = user.id,
                content = "      Content With Whitespace       ",
                imageFiles = listOf(DefaultInput.imageFiles[0])
            )
        )

        val createdComment = commentRepository.findById(targetCommentId).get()

        createdComment.id shouldBe targetCommentId
        createdComment.content shouldBe "Content With Whitespace"
    }

    @DisplayName("댓글 생성 : threadId에 해당하는 쓰레드가 없다면 예외가 발생한다")
    @Transactional
    @Test
    fun createCommentThreadNotFoundException() {
        shouldThrowExactly<DataNotFoundException> {
            chatService.createComment(
                command = DefaultCommand.CreateComment.copy(threadId = 9999999)
            )
        }
    }

    @DisplayName("댓글 생성 : 프로젝트와 관련없는 사용자(task 없는 경우)가 댓글을 삭제하려고 하면 예외가 발생한다.")
    @Transactional
    @Test
    fun createCommentTaskNotFound() {
        database.getUser()
        database.getThread()
        shouldThrowExactly<DataNotFoundException> {
            chatService.createComment(
                command = DefaultCommand.CreateComment
            )
        }
    }

    @DisplayName("댓글 생성 : GUEST는 본인이 생성한 쓰레드 내부에 댓글을 생성할 수 있다.")
    @Transactional
    @Test
    fun createCommentGuestCommentInOwnerThreadOK() {
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
        database.flush()
        val result = chatService.createComment(command = DefaultCommand.CreateComment)

        val createdComment = commentRepository.findById(1).get()

        result shouldBe true
        createdComment.id shouldBe 1
        createdComment.threadId shouldBe 1
        createdComment.userId shouldBe user.id
        createdComment.content shouldBe "New Comment"
    }

    @DisplayName("댓글 생성 : GUEST 본인이 작성하지 않은 타인의 쓰레드 내부에 댓글을 생성하려고 하면 예외가 발생한다.")
    @Transactional
    @Test
    fun createCommentGuestOtherThreadNotAllowedException() {
        val users = database.getUsers()
        database.getProject()
        taskRepository.save(
            DefaultInput.task.copy(userId = users[0].id, userState = UserState.LEADER)
        )
        val guest = taskRepository.save(
            DefaultInput.task.copy(userId = users[1].id, userState = UserState.GUEST)
        )
        val otherThread = database.getThread()
        chatService.createThread(
            command = DefaultCommand.CreateThread.copy(userId = guest.userId)
        )
        database.flush()
        shouldThrowExactly<NotAllowedException> {
            chatService.createComment(
                command = DefaultCommand.CreateComment.copy(
                    threadId = otherThread.id,
                    userId = guest.userId
                )
            )
        }
    }

    @DisplayName("댓글 생성 : 그만둔 사용자(QUIT)가 댓글을 생성하려고 하면 예외가 발생한다.")
    @Transactional
    @Test
    fun createCommentQuitTaskNotAllowedException() {
        val user = database.getUser()
        database.getProject()
        val thread = database.getThread()
        val quitTask = taskRepository.save(
            DefaultInput.task.copy(userId = user.id, userState = UserState.QUIT)
        )
        database.flush()
        shouldThrowExactly<NotAllowedException> {
            chatService.createComment(
                command = DefaultCommand.CreateComment.copy(
                    threadId = thread.id,
                    userId = quitTask.userId
                )
            )
        }
    }

    @DisplayName("댓글 수정 : commentId에 해당하는 댓글의 작성자는 댓글을 수정할 수 있다.")
    @Transactional
    @Test
    fun editCommentContentOK() {
        val user = database.getUser()
        val thread = database.getThread()
        taskRepository.save(DefaultInput.task)
        chatService.createComment(
            command = DefaultCommand.CreateComment.copy(threadId = thread.id, userId = user.id)
        )
        Thread.sleep(10)
        val createdComment = commentRepository.findById(1).get().copy()
        val result = chatService.editCommentContent(
            command = DefaultCommand.EditCommentContent.copy(
                commentId = 1,
                userId = user.id,
            )
        )
        database.flush()
        val editedComment = commentRepository.findById(1).get()

        result shouldBe true
        createdComment.content shouldBe "New Comment"
        editedComment.content shouldBe "edited Content"
        editedComment.createdAt shouldNotBe editedComment.modifiedAt
    }

    @DisplayName("댓글 수정 : 이미지가 달린 댓글의 content를 빈 문자열로 수정시 null로 수정된다")
    @Transactional
    @Test
    fun editCommentContentBlankInputBecomesNullOK() {
        val targetCommentId = 1L
        val user = database.getUser()
        database.getThread()
        val prevComment = database.getComment().copy()
        database.getImages()

        chatService.editCommentContent(
            command = DefaultCommand.EditCommentContent.copy(
                commentId = targetCommentId,
                userId = user.id,
                content = "             ",
            )
        )
        database.flushAndClear()
        val editedComment = commentRepository.findById(targetCommentId).get()

        prevComment.id shouldBe targetCommentId
        prevComment.content shouldBe DefaultDataInfo.comment.content
        editedComment.content shouldBe null
    }

    @DisplayName("댓글 수정 : 댓글의 content 수정시 좌우 공백은 제거되어 수정된다")
    @Transactional
    @Test
    fun editCommentContentWhiteSpaceTrimOK() {
        val targetCommentId = 1L
        val user = database.getUser()
        database.getThread()
        val prevComment = database.getComment().copy()
        database.getImages()

        chatService.editCommentContent(
            command = DefaultCommand.EditCommentContent.copy(
                commentId = targetCommentId,
                userId = user.id,
                content = "      Content With Whitespace       ",
            )
        )
        database.flushAndClear()
        val editedComment = commentRepository.findById(targetCommentId).get()

        prevComment.id shouldBe targetCommentId
        prevComment.content shouldBe DefaultDataInfo.comment.content
        editedComment.content shouldBe "Content With Whitespace"
    }

    @DisplayName("댓글 수정 : 이미지가 없는 댓글의 content가 null이 되면 해당 댓글은 삭제된다")
    @Transactional
    @Test
    fun editCommentContentDeletesEmptyCommentOK() {
        val targetCommentId = 1L
        val user = database.getUser()
        database.getThread()
        val prevComment = database.getComment()

        chatService.editCommentContent(
            command = DefaultCommand.EditCommentContent.copy(
                commentId = targetCommentId,
                userId = user.id,
                content = "                      ",
            )
        )
        database.flushAndClear()
        val editedComment = commentRepository.findById(targetCommentId)

        prevComment.id shouldBe targetCommentId
        prevComment.content shouldBe DefaultDataInfo.comment.content
        editedComment shouldBe Optional.empty()
    }

    @DisplayName("댓글 수정 : commentId에 해당하는 댓글이 없다면 예외가 발생한다.")
    @Transactional
    @Test
    fun editCommentCommentNotFoundException() {
        shouldThrowExactly<DataNotFoundException> {
            chatService.editCommentContent(
                command = DefaultCommand.EditCommentContent.copy(
                    commentId = 999999,
                )
            )
        }
    }

    @DisplayName("댓글 수정 : 수정하려는 댓글의 작성자가 아니면 예외가 발생한다.")
    @Transactional
    @Test
    fun editCommentNotCreatorException() {
        val user = database.getUser()
        val thread = database.getThread()
        taskRepository.save(DefaultInput.task)
        chatService.createComment(
            command = DefaultCommand.CreateComment.copy(threadId = thread.id, userId = user.id)
        )
        shouldThrowExactly<NotAllowedException> {
            chatService.editCommentContent(
                command = DefaultCommand.EditCommentContent.copy(
                    commentId = 1,
                    userId = 9999999999,
                )
            )
        }
    }

    @DisplayName("댓글 수정 : 이전과 동일한 content로 수정하려고 하면 예외가 발생한다.")
    @Transactional
    @Test
    fun editCommentSameContentException() {
        val user = database.getUser()
        val thread = database.getThread()
        taskRepository.save(DefaultInput.task)
        chatService.createComment(
            command = DefaultCommand.CreateComment.copy(
                threadId = thread.id,
                userId = user.id,
                content = "The Same Content"
            )
        )
        shouldThrowExactly<InvalidRequestException> {
            chatService.editCommentContent(
                command = DefaultCommand.EditCommentContent.copy(
                    commentId = 1,
                    userId = user.id,
                    content = "The Same Content"
                )
            )
        }
    }

    @DisplayName("댓글 이미지 삭제 : commentId에 해당하는 댓글의 작성자는 imageId에 해당하는 이미지를 댓글에서 삭제할 수 있다.")
    @Transactional
    @Test
    fun deleteCommentImageOK() {
        val user = database.getUser()
        taskRepository.save(DefaultInput.task)
        val thread = database.getThread()
        chatService.createComment(command = DefaultCommand.CreateComment.copy(threadId = thread.id, userId = user.id))
        val comment = commentRepository.findById(1).get().copy()
        val prevDBImages = database.getImages()
        val prevCommentImages = imageRepository.findByParentIdAndType(comment.id, ImageType.COMMENT)
        chatService.deleteCommentImage(
            command = DefaultCommand.DeleteCommentImage.copy(
                imageId = prevCommentImages[0].id,
                commentId = comment.id,
                userId = user.id
            )
        )
        val remainingDBImages = imageRepository.findAll()
        val remainingCommentImages = imageRepository.findByParentIdAndType(comment.id, ImageType.COMMENT)

        remainingDBImages.size shouldBe prevDBImages.size - 1
        remainingCommentImages.size shouldBe prevCommentImages.size - 1
        for (image in remainingCommentImages) {
            image.id shouldNotBe prevCommentImages[0].id
        }
    }

    @DisplayName("댓글 이미지 삭제 : content가 없는 댓글의 마지막 image 삭제시 댓글 자체가 삭제된다.")
    @Transactional
    @Test
    fun deleteCommentImageDeletesEmptyCommentOK() {
        val user = database.getUser()
        val thread = database.getThread()
        val comment = commentRepository.save(
            CommentEntity(
                threadId = thread.id,
                userId = user.id,
                content = null,
            )
        )
        val prevDBImages = database.getImages()
        val prevCommentImages = imageRepository.findByParentIdAndType(comment.id, ImageType.COMMENT)
        for (commentImage in prevCommentImages) {
            chatService.deleteCommentImage(
                command = DefaultCommand.DeleteCommentImage.copy(
                    imageId = commentImage.id,
                    commentId = comment.id,
                    userId = user.id
                )
            )
        }
        database.flushAndClear()
        val deletedComment = commentRepository.findById(comment.id)
        val remainingDBImages = imageRepository.findAll()
        val remainingCommentImages = imageRepository.findByParentIdAndType(comment.id, ImageType.COMMENT)

        deletedComment shouldBe Optional.empty()
        remainingDBImages.size shouldBe prevDBImages.size - 3
        prevCommentImages.size shouldBe 3
        remainingCommentImages.size shouldBe 0
        remainingCommentImages.map { it.id } shouldNotContainAnyOf prevCommentImages.map { it.id }
    }

    @DisplayName("댓글 이미지 삭제 : imageId에 해당하는 이미지가 없다면 예외가 발생한다.")
    @Transactional
    @Test
    fun deleteCommentImageImageNotFoundException() {
        database.getImages()
        shouldThrowExactly<DataNotFoundException> {
            chatService.deleteCommentImage(
                command = DefaultCommand.DeleteCommentImage.copy(
                    imageId = 9999999999,
                )
            )
        }
    }

    @DisplayName("댓글 이미지 삭제 : commentId에 해당하는 댓글이 없다면 예외가 발생한다.")
    @Transactional
    @Test
    fun deleteCommentImageThreadNotFoundException() {
        val images = database.getImages()
        shouldThrowExactly<DataNotFoundException> {
            chatService.deleteCommentImage(
                command = DefaultCommand.DeleteCommentImage.copy(
                    commentId = 999999999,
                    imageId = images[0].id,
                )
            )
        }
    }

    @DisplayName("댓글 이미지 삭제 : 해당 댓글의 작성자가 아니라면 예외가 발생한다.")
    @Transactional
    @Test
    fun deleteCommentImageNotCreatorException() {
        database.getUser()
        database.getProject()
        val comment = database.getComment()
        val images = database.getImages()
        shouldThrowExactly<NotAllowedException> {
            chatService.deleteCommentImage(
                command = DefaultCommand.DeleteCommentImage.copy(
                    imageId = images[6].id,
                    commentId = comment.id,
                    userId = 999999999999999,
                )
            )
        }
    }

    @DisplayName("댓글 이미지 삭제 : commentId와 이미지의 parentId가 불일치하면 예외가 발생한다.")
    @Transactional
    @Test
    fun deleteCommentImageInvalidThreadIdException() {
        database.getUser()
        database.getProject()
        database.getComment()
        val image = imageRepository.save(ImageEntity(type = ImageType.COMMENT, parentId = 999999999))

        shouldThrowExactly<InvalidRequestException> {
            chatService.deleteCommentImage(
                command = DefaultCommand.DeleteCommentImage.copy(
                    imageId = image.id
                )
            )
        }
    }

    @DisplayName("댓글 삭제 : commentId에 해당하는 댓글의 작성자는 댓글을 삭제할 수 있다.")
    @Transactional
    @Test
    fun deleteCommentOK() {
        val user = database.getUser()
        val thread = database.getThread()
        taskRepository.save(DefaultInput.task)
        chatService.createComment(
            command = DefaultCommand.CreateComment.copy(threadId = thread.id, userId = user.id)
        )
        val result = chatService.deleteComment(
            command = DeleteComment(commentId = 1, userId = user.id)
        )
        database.flushAndClear()
        val remainingThread = threadRepository.findById(thread.id).get()
        val deletedComment = commentRepository.findById(1)

        result shouldBe true
        deletedComment shouldBe Optional.empty()
        remainingThread.content shouldNotBe null
    }

    @DisplayName("댓글 삭제 : 삭제한 댓글이 달려있던 쓰레드에 다른 댓글도 없고 내용이 비어있다면 쓰레드를 삭제한다.")
    @Transactional
    @Test
    fun deleteCommentDeletesEmptyThreadOK() {
        val user = database.getUser()
        taskRepository.save(DefaultInput.task)
        val thread = database.getThread()
        chatService.createComment(
            command = DefaultCommand.CreateComment.copy(threadId = thread.id, userId = user.id)
        )
        chatService.deleteThread(
            command = DeleteThread(threadId = thread.id, userId = user.id)
        )
        database.flushAndClear()
        val emptyParentThread = threadRepository.findById(thread.id).get()
        val result = chatService.deleteComment(
            command = DeleteComment(commentId = 1, userId = user.id)
        )
        database.flushAndClear()
        val deletedParentThread = threadRepository.findById(thread.id)
        val deletedComment = commentRepository.findById(1)

        result shouldBe true
        deletedComment shouldBe Optional.empty()
        emptyParentThread.content shouldBe null
        deletedParentThread shouldBe Optional.empty()
    }

    @DisplayName("댓글 삭제 : commentId에 해당하는 댓글이 없다면 예외가 발생한다.")
    @Transactional
    @Test
    fun deleteCommentNotFoundException() {
        shouldThrowExactly<DataNotFoundException> {
            chatService.deleteComment(
                command = DefaultCommand.DeleteComment.copy(
                    commentId = 9999999999,
                )
            )
        }
    }

    @DisplayName("댓글 삭제 : 삭제하려는 댓글의 작성자가 아니면 예외가 발생한다.")
    @Transactional
    @Test
    fun deleteCommentNotCreatorException() {
        val user = database.getUser()
        val thread = database.getThread()
        taskRepository.save(DefaultInput.task)
        chatService.createComment(
            command = DefaultCommand.CreateComment.copy(threadId = thread.id, userId = user.id)
        )
        shouldThrowExactly<NotAllowedException> {
            chatService.deleteComment(
                command = DefaultCommand.DeleteComment.copy(
                    userId = 999999999999999,
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
