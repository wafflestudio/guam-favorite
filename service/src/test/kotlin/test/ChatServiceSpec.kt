package waffle.guam.test

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.transaction.annotation.Transactional
import waffle.guam.Database
import waffle.guam.DatabaseTest
import waffle.guam.db.entity.ImageType
import waffle.guam.db.repository.CommentRepository
import waffle.guam.db.repository.ProjectRepository
import waffle.guam.db.repository.ThreadRepository
import waffle.guam.db.repository.ThreadViewRepository
import waffle.guam.db.repository.ImageRepository
import waffle.guam.exception.DataNotFoundException
import waffle.guam.exception.InvalidRequestException
import waffle.guam.model.ThreadDetail
import waffle.guam.model.ThreadOverView
import waffle.guam.service.ChatService
import waffle.guam.service.command.CreateThread
import waffle.guam.service.command.CreateComment
import waffle.guam.service.command.DeleteThreadImage
import waffle.guam.service.command.EditThreadContent
import waffle.guam.service.command.DeleteThread
import waffle.guam.service.command.EditCommentContent
import waffle.guam.service.command.DeleteCommentImage
import waffle.guam.service.command.DeleteComment
import java.util.Optional

@DatabaseTest
class ChatServiceSpec @Autowired constructor(
    private val threadRepository: ThreadRepository,
    private val threadViewRepository: ThreadViewRepository,
    private val commentRepository: CommentRepository,
    private val projectRepository: ProjectRepository,
    private val imageRepository: ImageRepository,
    private val database: Database
) {
    private val chatService = ChatService(
        threadRepository = threadRepository,
        threadViewRepository = threadViewRepository,
        commentRepository = commentRepository,
        projectRepository = projectRepository,
        imageRepository = imageRepository
    )

    @BeforeEach
    fun clearDatabase() {
        database.cleanUp()
    }

    @DisplayName("projectId에 해당하는 프로젝트에 달린 쓰레드들을 조회할 수 있다")
    @Transactional
    @Test
    fun getThreadsOK() {
        database.getUser()
        val project = database.getProject()
        for (i in 0 until 15) {
            chatService.createThread(
                command = DefaultCommand.CreateThread.copy(content = "Thread Number $i", imageUrls = listOf("Thread $i imageUrl1", "Thread $i imageUrl2", "Thread $i imageUrl3"))
            )
        }
        for (i in 0 until 5) {
            chatService.createComment(
                command = DefaultCommand.CreateComment.copy(threadId = 1, content = "Comment Number $i", imageUrls = listOf("Thread 1 Comment $i imageUrl1", "Thread 1 Comment $i imageUrl2", "Thread 1 Comment $i imageUrl3"))
            )
            chatService.createComment(
                command = DefaultCommand.CreateComment.copy(threadId = 2, content = "Comment Number $i", imageUrls = listOf())
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

    @DisplayName("projectId, page, size 정보에 해당되는 쓰레드들의 세부 정보를 조회할 수 있다")
    @Transactional
    @Test
    fun getThreadsDetailsOK() {

        // 프로젝트 1개
        // 사용자 2명 - 프사O vs 프사X
        // 쓰레드 15개 - 댓글O vs 댓글X
        // 댓글 5개

//        database.getUser()
//        database.getUserProfiles()
//        val project = database.getProject()
//        for (i in 0 until 15) {
//            chatService.createThread(
//                command = DefaultCommand.CreateThread.copy(content = "Thread Number $i", imageUrls = listOf("Thread $i imageUrl1", "Thread $i imageUrl2", "Thread $i imageUrl3"))
//            )
//        }
//        for (i in 0 until 5) {
//            chatService.createComment(
//                command = DefaultCommand.CreateComment.copy(threadId = 1, content = "Comment Number $i", imageUrls = listOf("Thread 1 Comment $i imageUrl1", "Thread 1 Comment $i imageUrl2", "Thread 1 Comment $i imageUrl3"))
//            )
//            chatService.createComment(
//                command = DefaultCommand.CreateComment.copy(threadId = 2, content = "Comment Number $i", imageUrls = listOf())
//            )
//        }
//        val result: Page<ThreadOverView> = chatService.getThreads(
//            projectId = project.id,
//            pageable = PageRequest.of(
//                0,
//                10,
//            )
//        )
//        println(result.content)
//
//        result.content.size shouldBe 10
//        result.content[0].content shouldBe "Thread Number 0"
//        result.content[0].commentSize shouldBe 5
//        result.content[5].content shouldBe "Thread Number 5"
//        result.content[5].commentSize shouldBe 0
//        result.pageable.offset shouldBe 0
//        result.totalElements shouldBe 15
    }


    @DisplayName("page와 size에 해당되는 범위에 쓰레드가 없어도 예외는 발생하지 않는다")
    @Transactional
    @Test
    fun getThreadsOutOfRange() {
        val totalThreadNum = 15
        val page = 100
        val size = 100
        database.getUser()
        val project = database.getProject()
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

    @DisplayName("projectId에 해당하는 프로젝트가 없어도 예외는 발생하지 않는다")
    @Transactional
    @Test
    fun getThreadsProjectNotFound() {
        val totalThreadNum = 0
        val page = 0
        val size = 10
        val result = chatService.getThreads(
            projectId = 999999,
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

    @DisplayName("threadId에 해당하는 쓰레드를 찾아준다")
    @Transactional
    @Test
    fun getFullThreadOK() {
        val user = database.getUser()
        val project = database.getProject()
        chatService.createThread(
            command = DefaultCommand.CreateThread.copy(projectId = project.id, userId = user.id)
        )
        chatService.createComment(
            command = DefaultCommand.CreateComment.copy(content = "First", userId = user.id)
        )
        chatService.createComment(
            command = DefaultCommand.CreateComment.copy(content = "Second", userId = user.id)
        )
        val result: ThreadDetail = chatService.getFullThread(1)
        result.id shouldBe 1
        result.creatorId shouldBe user.id
        // TODO(result.creator.status shouldBe user.status)
        result.creatorImageUrl shouldBe null
        result.creatorNickname shouldBe user.nickname
        result.content shouldBe DefaultCommand.CreateThread.content
        result.comments[0].content shouldBe "First"
        result.comments[0].creatorNickname shouldBe user.nickname
        result.comments[1].content shouldBe "Second"
        result.comments[1].creatorNickname shouldBe user.nickname
    }

    @DisplayName("threadId에 해당하는 쓰레드가 없다면 예외가 발생한다")
    @Transactional
    @Test
    fun getFullThreadThreadNotFoundException() {
        shouldThrowExactly<RuntimeException> {
            chatService.getFullThread(99999999999)
        }
    }

    @DisplayName("projectId에 해당하는 프로젝트에 content 정보로 쓰레드를 생성한다")
    @Transactional
    @Test
    fun createdThreadWithContentOK() {
        val user = database.getUser()
        val project = database.getProject()

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

    @DisplayName("projectId에 해당하는 프로젝트에 imageUrl 배열 정보로 쓰레드를 생성한다")
    @Transactional
    @Test
    fun createdThreadWithImageUrlsOK() {
    }

    @DisplayName("projectId에 해당하는 프로젝트에 content와 imageUrl 배열 정보로 쓰레드를 생성한다")
    @Transactional
    @Test
    fun createdThreadWithContentAndImageUrlsOK() {
    }

    @DisplayName("쓰레드 생성시 content와 imageUrl 배열 정보가 null이라면 예외가 발생한다")
    @Transactional
    @Test
    fun createdThreadNullInputException() {
        shouldThrowExactly<InvalidRequestException> {
            chatService.createThread(
                command = DefaultCommand.CreateThread.copy(content = null, imageUrls = null)
            )
        }
    }

    @DisplayName("projectId에 해당하는 프로젝트가 없다면 예외가 발생한다")
    @Transactional
    @Test
    fun createdThreadThreadNotFoundException() {
        shouldThrowExactly<DataNotFoundException> {
            chatService.createThread(
                command = DefaultCommand.CreateThread.copy(projectId = 9999999)
            )
        }
    }

    @DisplayName("threadId에 해당하는 쓰레드의 작성자는 쓰레드를 수정할 수 있다.")
    @Transactional
    @Test
    fun editThreadOK() {
        val user = database.getUser()
        val project = database.getProject()
        chatService.createThread(
            command = DefaultCommand.CreateThread.copy(projectId = project.id, userId = user.id)
        )
        val createdThread = threadRepository.findById(1).get().copy()

        val result = chatService.editThreadContent(
            command = DefaultCommand.EditThreadContent.copy(
                threadId = 1,
                userId = user.id,
            )
        )
        val editedThread = threadRepository.findById(1).get()

        result shouldBe true
        createdThread.content shouldBe "New Thread"
        editedThread.content shouldBe "edited Content"
        editedThread.createdAt shouldNotBe editedThread.modifiedAt
    }

    @DisplayName("threadId에 해당하는 쓰레드가 없다면 예외가 발생한다.")
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

    @DisplayName("수정하려는 쓰레드의 작성자가 아니면 예외가 발생한다.")
    @Transactional
    @Test
    fun editThreadNotCreatorException() {
        val user = database.getUser()
        val project = database.getProject()
        chatService.createThread(
            command = DefaultCommand.CreateThread.copy(projectId = project.id, userId = user.id)
        )
        shouldThrowExactly<InvalidRequestException> {
            chatService.editThreadContent(
                command = DefaultCommand.EditThreadContent.copy(
                    userId = 999999,
                )
            )
        }
    }

    @DisplayName("이전과 동일한 content로 수정하려고 하면 예외가 발생한다.")
    @Transactional
    @Test
    fun editThreadSameContentException() {
        val user = database.getUser()
        val project = database.getProject()
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

    @DisplayName("threadId에 해당하는 쓰레드의 작성자는 imageId에 해당하는 이미지를 쓰레드에서 삭제할 수 있다.")
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
        val remainingDBImages = imageRepository.findAll()
        val remainingThreadImages = imageRepository.findByParentIdAndType(thread.id, ImageType.THREAD)

        remainingDBImages.size shouldBe prevDBImages.size - 1
        remainingThreadImages.size shouldBe prevThreadImages.size - 1
        for(image in remainingThreadImages){
            image.id shouldNotBe prevThreadImages[0].id
        }
    }

    @DisplayName("threadId에 해당하는 쓰레드의 작성자는 쓰레드를 삭제할 수 있다.")
    @Transactional
    @Test
    fun deleteThreadOK() {
        val user = database.getUser()
        val project = database.getProject()
        chatService.createThread(
            command = DefaultCommand.CreateThread.copy(projectId = project.id, userId = user.id)
        )
        val result = chatService.deleteThread(
            command = DefaultCommand.DeleteThread.copy(
                threadId = 1,
            )
        )
        val deletedThread = threadRepository.findById(1)
        result shouldBe true
        deletedThread shouldBe Optional.empty()
    }

    @DisplayName("쓰레드를 삭제하면 쓰레드에 달린 댓글들도 자동 삭제된다.")
    @Transactional
    @Test
    fun deleteThreadWithCommentsOK() {
        val user = database.getUser()

        val project = database.getProject()
        chatService.createThread(
            command = DefaultCommand.CreateThread.copy(projectId = project.id, userId = user.id)
        )
        chatService.createThread(
            command = DefaultCommand.CreateThread.copy(projectId = project.id, userId = user.id)
        )
        for (i in 0 until 3) {
            chatService.createComment(
                command = DefaultCommand.CreateComment.copy(
                    threadId = 1, userId = 1, content = "Should be Deleted"
                )
            )
            chatService.createComment(
                command = DefaultCommand.CreateComment.copy(
                    threadId = 2, userId = 1, content = "Should Not be Deleted"
                )
            )
        }
        val result = chatService.deleteThread(
            command = DefaultCommand.DeleteThread.copy(
                threadId = 1,
            )
        )
        val remainingCommentsInDeletedThread = commentRepository.countByThreadId(1)
        val remainingCommentsInAnotherThread = commentRepository.countByThreadId(2)
        result shouldBe true
        remainingCommentsInDeletedThread shouldBe 0
        remainingCommentsInAnotherThread shouldBe 3
    }

    @DisplayName("threadId에 해당하는 쓰레드가 없다면 예외가 발생한다.")
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

    @DisplayName("삭제하려는 쓰레드의 작성자가 아니면 예외가 발생한다.")
    @Transactional
    @Test
    fun deleteThreadNotCreatorException() {
        val user = database.getUser()
        val project = database.getProject()
        chatService.createThread(
            command = DefaultCommand.CreateThread.copy(projectId = project.id, userId = user.id)
        )
        shouldThrowExactly<InvalidRequestException> {
            chatService.deleteThread(
                command = DefaultCommand.DeleteThread.copy(
                    userId = 999999999999999,
                )
            )
        }
    }

    @DisplayName("threadId에 해당하는 쓰레드에 content 정보로 댓글을 생성한다.")
    @Transactional
    @Test
    fun createCommentWithContentOK() {
        val user = database.getUser()
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

    @DisplayName("threadId에 해당하는 쓰레드가 없다면 예외가 발생한다")
    @Transactional
    @Test
    fun createCommentThreadNotFoundException() {
        shouldThrowExactly<DataNotFoundException> {
            chatService.createComment(
                command = DefaultCommand.CreateComment.copy(threadId = 9999999)
            )
        }
    }

    @DisplayName("commentId에 해당하는 댓글의 작성자는 댓글을 수정할 수 있다.")
    @Transactional
    @Test
    fun editCommentOK() {
        val user = database.getUser()
        val thread = database.getThread()
        chatService.createComment(
            command = DefaultCommand.CreateComment.copy(threadId = thread.id, userId = user.id)
        )
        val createdComment = commentRepository.findById(1).get().copy()
        val result = chatService.editCommentContent(
            command = DefaultCommand.EditCommentContent.copy(
                commentId = 1,
                userId = user.id,
            )
        )
        val editedComment = commentRepository.findById(1).get()

        result shouldBe true
        createdComment.content shouldBe "New Comment"
        editedComment.content shouldBe "edited Content"
        editedComment.createdAt shouldNotBe editedComment.modifiedAt
    }

    @DisplayName("commentId에 해당하는 댓글이 없다면 예외가 발생한다.")
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

    @DisplayName("수정하려는 댓글의 작성자가 아니면 예외가 발생한다.")
    @Transactional
    @Test
    fun editCommentNotCreatorException() {
        val user = database.getUser()
        val thread = database.getThread()
        chatService.createComment(
            command = DefaultCommand.CreateComment.copy(threadId = thread.id, userId = user.id)
        )
        shouldThrowExactly<InvalidRequestException> {
            chatService.editCommentContent(
                command = DefaultCommand.EditCommentContent.copy(
                    commentId = 1,
                    userId = 9999999999,
                )
            )
        }
    }

    @DisplayName("이전과 동일한 content로 수정하려고 하면 예외가 발생한다.")
    @Transactional
    @Test
    fun editCommentSameContentException() {
        val user = database.getUser()
        val thread = database.getThread()
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

    @DisplayName("commentId에 해당하는 댓글의 작성자는 imageId에 해당하는 이미지를 댓글에서 삭제할 수 있다.")
    @Transactional
    @Test
    fun deleteCommentImageOK() {
        val user = database.getUser()
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
        for(image in remainingCommentImages){
            image.id shouldNotBe prevCommentImages[0].id
        }
    }

    @DisplayName("commentId에 해당하는 댓글의 작성자는 댓글을 삭제할 수 있다.")
    @Transactional
    @Test
    fun deleteCommentOK() {
        val user = database.getUser()
        val thread = database.getThread()
        chatService.createComment(
            command = DefaultCommand.CreateComment.copy(threadId = thread.id, userId = user.id)
        )
        val result = chatService.deleteComment(
            command = DefaultCommand.DeleteComment.copy(
                commentId = 1,
            )
        )
        val deletedComment = commentRepository.findById(1)
        result shouldBe true
        deletedComment shouldBe Optional.empty()
    }

    @DisplayName("commentId에 해당하는 댓글이 없다면 예외가 발생한다.")
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

    @DisplayName("삭제하려는 댓글의 작성자가 아니면 예외가 발생한다.")
    @Transactional
    @Test
    fun deleteCommentNotCreatorException() {
        val user = database.getUser()
        val thread = database.getThread()
        chatService.createComment(
            command = DefaultCommand.CreateComment.copy(threadId = thread.id, userId = user.id)
        )
        shouldThrowExactly<InvalidRequestException> {
            chatService.deleteComment(
                command = DefaultCommand.DeleteComment.copy(
                    userId = 999999999999999,
                )
            )
        }
    }
}

object DefaultCommand {
    val CreateThread = CreateThread(
        projectId = 1,
        userId = 1,
        content = "New Thread",
        imageUrls = listOf("imageUrl1", "imageUrl2", "imageUrl3")
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
        imageUrls = listOf("imageUrl1", "imageUrl2", "imageUrl3")
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
