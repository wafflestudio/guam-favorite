package waffle.guam.comment

import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.shouldBe
import waffle.guam.annotation.DatabaseTest
import waffle.guam.task.TaskService
import waffle.guam.thread.ThreadRepository
import waffle.guam.user.UserRepository

@DatabaseTest(["comment/image.sql", "comment/user.sql", "comment/thread.sql", "comment/comment.sql"])
class CommentServiceQueryTest(
    commentRepository: CommentRepository,
    threadRepository: ThreadRepository,
    taskService: TaskService,
    userRepository: UserRepository
) : FeatureSpec() {
    private val commentService = CommentServiceImpl(
        commentRepository,
        threadRepository,
        taskService,
        userRepository
    )

    init {
        feature("댓글 조회") {
            scenario("댓글에 담긴 모든 정보들을 조회한다.") {
                val result = commentService.getComment(1L)

                result.id shouldBe 1L
                result.content shouldBe "댓글 내용 1"
                result.creatorNickname shouldBe "사용자 1"
                result.creatorImageUrl shouldBe "PROFILE/101"
                result.commentImages.size shouldBe 3
                result.commentImages[0].path shouldBe "COMMENT/109"
                result.createdAt shouldBeLessThanOrEqualTo result.modifiedAt
            }

            scenario("댓글의 내용이 빈 문자열이어도 문제 없이 조회된다.") {
                val result = commentService.getComment(2L)

                result.id shouldBe 2L
                result.content shouldBe null
                result.creatorNickname shouldBe "사용자 1"
                result.creatorImageUrl shouldBe "PROFILE/101"
                result.commentImages.size shouldBe 1
                result.commentImages[0].path shouldBe "COMMENT/112"
                result.createdAt shouldBeLessThanOrEqualTo result.modifiedAt
            }

            scenario("댓글에 이미지가 없어도 문제 없이 조회된다.") {
                val result = commentService.getComment(3L)

                result.id shouldBe 3L
                result.content shouldBe "댓글 내용 3"
                result.creatorNickname shouldBe "사용자 1"
                result.creatorImageUrl shouldBe "PROFILE/101"
                result.commentImages.size shouldBe 0
                result.createdAt shouldBeLessThanOrEqualTo result.modifiedAt
            }
        }
    }
}
