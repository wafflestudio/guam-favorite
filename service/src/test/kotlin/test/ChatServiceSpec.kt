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
import waffle.guam.db.repository.CommentRepository
import waffle.guam.db.repository.ProjectRepository
import waffle.guam.db.repository.ThreadRepository
import waffle.guam.db.repository.ThreadViewRepository
import waffle.guam.exception.DataNotFoundException
import waffle.guam.exception.InvalidRequestException
import waffle.guam.model.ThreadDetail
import waffle.guam.model.ThreadOverView
import waffle.guam.service.ChatService
import waffle.guam.service.command.CreateComment
import waffle.guam.service.command.CreateThread
import waffle.guam.service.command.DeleteComment
import waffle.guam.service.command.DeleteThread
import waffle.guam.service.command.EditCommentContent
import waffle.guam.service.command.EditThreadContent
import java.util.Optional

@DatabaseTest
class ChatServiceSpec @Autowired constructor(
    private val threadRepository: ThreadRepository,
    private val threadViewRepository: ThreadViewRepository,
    private val commentRepository: CommentRepository,
    private val projectRepository: ProjectRepository,
    private val database: Database
) {
    private val chatService = ChatService(
        threadRepository = threadRepository,
        threadViewRepository = threadViewRepository,
        commentRepository = commentRepository,
        projectRepository = projectRepository,
    )

    @BeforeEach
    fun clearDatabase() {
        database.cleanUp()
    }

    @DisplayName("projectId에 해당하는 프로젝트에 달린 쓰레드들을 찾아준다")
    @Transactional
    @Test
    fun getThreadsOK() {
        database.getUser()
        val project = database.getProject()
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

    @DisplayName("projectId에 해당하는 프로젝트에 쓰레드를 생성한다")
    @Transactional
    @Test
    fun createdThreadOK() {
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
            command = DefaultCommand.EditThread.copy(
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
                command = DefaultCommand.EditThread.copy(
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
                command = DefaultCommand.EditThread.copy(
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
                command = DefaultCommand.EditThread.copy(
                    content = "The Same Content"
                )
            )
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

    @DisplayName("threadId에 해당하는 쓰레드에 댓글을 생성한다.")
    @Transactional
    @Test
    fun createCommentOK() {
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
            command = DefaultCommand.EditComment.copy(
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
                command = DefaultCommand.EditComment.copy(
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
                command = DefaultCommand.EditComment.copy(
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
                command = DefaultCommand.EditComment.copy(
                    commentId = 1,
                    userId = user.id,
                    content = "The Same Content"
                )
            )
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
        content = "New Thread"
    )
    val EditThread = EditThreadContent(
        threadId = 1,
        userId = 1,
        content = "edited Content"
    )
    val DeleteThread = DeleteThread(
        threadId = 1,
        userId = 1,
    )
    val CreateComment = CreateComment(
        threadId = 1,
        userId = 1,
        content = "New Comment"
    )
    val EditComment = EditCommentContent(
        commentId = 1,
        userId = 1,
        content = "edited Content"
    )
    val DeleteComment = DeleteComment(
        commentId = 1,
        userId = 1,
    )
}