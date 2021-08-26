package waffle.guam.comment

import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import waffle.guam.annotation.DatabaseTest
import waffle.guam.project.ProjectRepository
import waffle.guam.task.TaskCandidateRepository
import waffle.guam.task.TaskHandler
import waffle.guam.task.TaskHistoryRepository
import waffle.guam.task.TaskRepository
import waffle.guam.task.TaskServiceImpl
import waffle.guam.thread.ThreadRepository
import waffle.guam.user.UserRepository

@DatabaseTest(["comment/image.sql", "comment/user.sql", "comment/thread.sql", "comment/comment.sql"])
class CommentServiceQueryTest @Autowired constructor(
    commentRepository: CommentRepository,
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

    @DisplayName("댓글 조회 : 댓글에 담긴 모든 정보들을 조회한다.")
    @Transactional
    @Test
    fun getCommentOK() {
        val result = commentService.getComment(1L)

        result.id shouldBe 1L
        result.content shouldBe "댓글 내용 1"
        result.creatorNickname shouldBe "사용자 1"
        result.creatorImageUrl shouldBe "PROFILE/101"
        result.commentImages.size shouldBe 3
        result.commentImages[0].path shouldBe "COMMENT/109"
        result.createdAt shouldBeLessThanOrEqualTo result.modifiedAt
    }

    @DisplayName("댓글 조회 : 댓글의 내용이 빈 문자열이면 null로 반환된다.")
    @Transactional
    @Test
    fun getCommentBlankContentOK() {
        val result = commentService.getComment(2L)

        result.id shouldBe 2L
        result.content shouldBe null
        result.creatorNickname shouldBe "사용자 1"
        result.creatorImageUrl shouldBe "PROFILE/101"
        result.commentImages.size shouldBe 1
        result.commentImages[0].path shouldBe "COMMENT/112"
        result.createdAt shouldBeLessThanOrEqualTo result.modifiedAt
    }

    @DisplayName("댓글 조회 : 댓글에 이미지가 없어도 문제 없이 조회된다.")
    @Transactional
    @Test
    fun getCommentNoImagesOK() {
        val result = commentService.getComment(3L)

        result.id shouldBe 3L
        result.content shouldBe "댓글 내용 3"
        result.creatorNickname shouldBe "사용자 1"
        result.creatorImageUrl shouldBe "PROFILE/101"
        result.commentImages.size shouldBe 0
        result.commentImages shouldBe listOf()
        result.createdAt shouldBeLessThanOrEqualTo result.modifiedAt
    }
}
