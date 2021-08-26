package waffle.guam.comment

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import waffle.guam.DataNotFoundException
import waffle.guam.InvalidRequestException
import waffle.guam.NotAllowedException
import waffle.guam.annotation.DatabaseTest
import waffle.guam.comment.command.CreateComment
import waffle.guam.comment.command.DeleteComment
import waffle.guam.comment.command.EditCommentContent
import waffle.guam.project.ProjectRepository
import waffle.guam.task.TaskCandidateRepository
import waffle.guam.task.TaskHandler
import waffle.guam.task.TaskHistoryRepository
import waffle.guam.task.TaskRepository
import waffle.guam.task.TaskServiceImpl
import waffle.guam.thread.ThreadRepository
import waffle.guam.user.UserRepository
import java.util.Optional

@DatabaseTest(["comment/image.sql", "comment/user.sql", "comment/project.sql", "comment/task_candidate.sql", "comment/task.sql", "comment/task_history.sql", "comment/thread.sql", "comment/comment.sql"])
class CommentServiceCommandTest @Autowired constructor(
    private val commentRepository: CommentRepository,
    threadRepository: ThreadRepository,
    userRepository: UserRepository,
    projectRepository: ProjectRepository,
    taskRepository: TaskRepository,
    taskCandidateRepository: TaskCandidateRepository,
    taskHistoryRepository: TaskHistoryRepository,
) {
    private val taskHandler = TaskHandler(
        taskRepository,
        taskCandidateRepository,
        taskHistoryRepository,
        userRepository,
        projectRepository,
    )

    private val taskService = TaskServiceImpl(
        taskRepository,
        taskCandidateRepository,
        taskHandler,
    )

    private val commentService = CommentServiceImpl(
        commentRepository,
        threadRepository,
        taskService,
        userRepository
    )

    // 주의: mocking 없이 MockMultipartFile 리스트 대입하면 S3에 그대로 업로드됨
    @DisplayName("댓글 생성 : 특정 쓰레드에 content 정보로 댓글을 생성한다.")
    @Transactional
    @Test
    fun createCommentOK() {
        val command = CreateComment(
            threadId = 1,
            userId = 1,
            content = "댓글 내용 4",
            imageFiles = null
        )

        val event = commentService.createComment(command)

        event.projectId shouldBe 1
        event.commentId shouldBe 4
        event.threadCreatorId shouldBe 1
        event.commentCreatorId shouldBe command.userId
        event.commentCreatorName shouldBe "사용자 1"
        event.content shouldBe command.content
        event.imageFiles shouldBe null

        val createdComment = commentService.getComment(4L)

        createdComment.id shouldBe 4L
        createdComment.content shouldBe command.content
        createdComment.isEdited shouldBe false
        createdComment.creatorId shouldBe 1
        createdComment.creatorNickname shouldBe "사용자 1"
        createdComment.creatorImageUrl shouldBe "PROFILE/101"
        createdComment.commentImages.size shouldBe 0
        createdComment.commentImages shouldBe listOf()
        createdComment.createdAt shouldBeLessThanOrEqualTo createdComment.modifiedAt
    }

    @DisplayName("댓글 생성 예외 : 존재하지 않는 쓰레드에 댓글을 생성 시도시 예외가 발생한다.")
    @Transactional
    @Test
    fun createCommentThreadNotFoundException() {
        shouldThrowExactly<DataNotFoundException> {
            commentService.createComment(
                command = CreateComment(
                    threadId = 999999999,
                    userId = 1,
                    content = "존재하지 않는 쓰레드에 댓글 달아보기",
                    imageFiles = null
                )
            )
        }
    }

    @DisplayName("댓글 생성 예외 : GUEST가 타인이 생성한 쓰레드에 댓글을 생성 시도시 예외가 발생한다.")
    @Transactional
    @Test
    fun createCommentGuestNotAllowedException() {
        shouldThrowExactly<NotAllowedException> {
            commentService.createComment(
                command = CreateComment(
                    threadId = 1,
                    userId = 2,
                    content = "본인의 참가 신청 쓰레드가 아닌 쓰레드에 댓글 달아보기",
                    imageFiles = null
                )
            )
        }
    }

    @DisplayName("댓글 생성 예외 : DECLINED된 사용자가 자신의 참가 신청 쓰레드에 댓글을 생성 시도시 예외가 발생한다.")
    @Transactional
    @Test
    fun createCommentDeclinedNotAllowedException() {
        shouldThrowExactly<NotAllowedException> {
            commentService.createComment(
                command = CreateComment(
                    threadId = 5,
                    userId = 2,
                    content = "탈퇴한 사용자가 자신의 조인 쓰레드에 댓글 달아보기",
                    imageFiles = null
                )
            )
        }
    }

    @DisplayName("댓글 생성 예외 : QUIT한 사용자가 쓰레드에 댓글을 생성 시도시 예외가 발생한다.")
    @Transactional
    @Test
    fun createCommentQuitNotAllowedException() {
        shouldThrowExactly<NotAllowedException> {
            commentService.createComment(
                command = CreateComment(
                    threadId = 1,
                    userId = 4,
                    content = "탈퇴한 사용자가 자신의 조인 쓰레드에 댓글 달아보기",
                    imageFiles = null
                )
            )
        }
    }

    @DisplayName("댓글 수정 : 특정 댓글의 content 정보를 수정한다.")
    @Transactional
    @Test
    fun editCommentContentOK() {
        val command = EditCommentContent(
            commentId = 1,
            userId = 1,
            content = "댓글 내용1 - 수정됨"
        )

        val event = commentService.editCommentContent(command)

        event.commentId shouldBe command.commentId

        val editedComment = commentService.getComment(1L)

        editedComment.id shouldBe 1L
        editedComment.content shouldBe command.content
        editedComment.isEdited shouldBe true
        editedComment.creatorId shouldBe 1
        editedComment.creatorNickname shouldBe "사용자 1"
        editedComment.creatorImageUrl shouldBe "PROFILE/101"
        editedComment.commentImages.size shouldBe 3
        editedComment.commentImages[0].path shouldBe "COMMENT/109"
        editedComment.createdAt shouldBeLessThanOrEqualTo editedComment.modifiedAt
    }

    @DisplayName("댓글 수정 예외 : 존재하지 않는 댓글을 수정 시도시 예외가 발생한다.")
    @Transactional
    @Test
    fun editCommentContentCommentNotFoundException() {
        shouldThrowExactly<DataNotFoundException> {
            commentService.editCommentContent(
                command = EditCommentContent(
                    commentId = 9999999999999,
                    userId = 1,
                    content = "존재하지 않는 댓글"
                )
            )
        }
    }

    @DisplayName("댓글 수정 예외 : 타인이 생성한 댓글을 수정 시도시 예외가 발생한다.")
    @Transactional
    @Test
    fun editCommentContentNotCreatorException() {
        shouldThrowExactly<NotAllowedException> {
            commentService.editCommentContent(
                command = EditCommentContent(
                    commentId = 1,
                    userId = 99999,
                    content = "댓글의 생성자가 아닌 경우"
                )
            )
        }
    }

    @DisplayName("댓글 수정 예외 : 기존 content와 동일한 내용으로 수정 시도시 예외가 발생한다.")
    @Transactional
    @Test
    fun editCommentContentNoChangeException() {
        shouldThrowExactly<InvalidRequestException> {
            commentService.editCommentContent(
                command = EditCommentContent(
                    commentId = 2,
                    userId = 1,
                    content = ""
                )
            )
        }
    }

    @DisplayName("댓글 삭제 : 사용자는 자신이 생성한 댓글을 삭제할 수 있다.")
    @Transactional
    @Test
    fun deleteCommentOK() {
        val command = DeleteComment(
            commentId = 3,
            userId = 1,
        )

        val event = commentService.deleteComment(command)

        event.commentId shouldBe command.commentId
        event.threadId shouldBe 1

        val deletedComment = commentRepository.findById(3L)

        deletedComment shouldBe Optional.empty()
    }

    @DisplayName("댓글 삭제 예외 : 존재하지 않는 댓글을 삭제 시도시 예외가 발생한다.")
    @Transactional
    @Test
    fun deleteCommentNotFoundException() {
        shouldThrowExactly<DataNotFoundException> {
            commentService.deleteComment(
                command = DeleteComment(
                    commentId = 9999999999999999,
                    userId = 1,
                )
            )
        }
    }

    @DisplayName("댓글 삭제 예외 : 타인이 생성한 댓글을 삭제 시도시 예외가 발생한다.")
    @Transactional
    @Test
    fun deleteCommentNotCreatorException() {
        shouldThrowExactly<NotAllowedException> {
            commentService.deleteComment(
                command = DeleteComment(
                    commentId = 1,
                    userId = 9999999999999999,
                )
            )
        }
    }
}
