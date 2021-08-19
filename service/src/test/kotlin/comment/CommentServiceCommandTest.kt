package waffle.guam.comment

import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import waffle.guam.annotation.DatabaseTest
import waffle.guam.comment.command.CreateComment
import waffle.guam.comment.command.DeleteComment
import waffle.guam.comment.command.EditCommentContent
import waffle.guam.task.TaskService
import waffle.guam.thread.ThreadRepository
import waffle.guam.user.UserRepository
import java.util.Optional

@DatabaseTest(["comment/image.sql", "comment/user.sql", "comment/project.sql", "comment/task.sql", "comment/thread.sql", "comment/comment.sql"])
class CommentServiceCommandTest @Autowired constructor(
    private val commentRepository: CommentRepository,
    private val threadRepository: ThreadRepository,
    private val taskService: TaskService,
    private val userRepository: UserRepository
) {
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
}
