package waffle.guam.thread

import com.amazonaws.services.s3.AmazonS3Client
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.transaction.annotation.Transactional
import waffle.guam.annotation.DatabaseTest
import waffle.guam.comment.CommentRepository
import waffle.guam.image.ImageRepository
import waffle.guam.image.ImageServiceImpl
import waffle.guam.project.ProjectRepository
import waffle.guam.task.TaskCandidateRepository
import waffle.guam.task.TaskHandler
import waffle.guam.task.TaskHistoryRepository
import waffle.guam.task.TaskRepository
import waffle.guam.task.TaskServiceImpl
import waffle.guam.user.UserRepository

@DatabaseTest(["thread/image.sql", "thread/user.sql", "thread/project.sql", "thread/thread.sql", "thread/comment.sql"])
class ThreadServiceQueryTest @Autowired constructor(
    threadRepository: ThreadRepository,
    threadViewRepository: ThreadViewRepository,
    projectRepository: ProjectRepository,
    commentRepository: CommentRepository,
    taskRepository: TaskRepository,
    taskCandidateRepository: TaskCandidateRepository,
    taskHistoryRepository: TaskHistoryRepository,
    userRepository: UserRepository,
    imageRepository: ImageRepository,
) {
    private val mockAwsClient: AmazonS3Client = mockk()

    private val imageService = ImageServiceImpl(
        imageRepository = imageRepository,
        projectRepository = projectRepository,
        userRepository = userRepository,
        client = mockAwsClient
    )

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

    private val threadService = ThreadServiceImpl(
        threadRepository,
        threadViewRepository,
        projectRepository,
        taskService,
        commentRepository,
        imageService,
    )

    @DisplayName("복수 쓰레드 조회 : 프로젝트에 달린 일부 쓰레드들의 정보만 조회한다.")
    @Transactional
    @Test
    fun getThreadsOK() {
        val result = threadService.getThreads(
            projectId = 1L,
            pageable = PageRequest.of(
                0,
                10,
                Sort.by("id").descending()
            )
        )

        // almost controller response format
        val data = result.content.asReversed()
        val size = result.content.size
        val offset = result.pageable.offset.toInt()
        val totalCount = result.totalElements.toInt()
        val hasNext = result.pageable.offset + result.size < result.totalElements

        data[0].id shouldBeLessThan data[1].id // id 오름차순으로 반환. 1,2,3,4,5

        data[0].content shouldBe "쓰레드 내용 1"
        data[0].threadImages.size shouldBe 3
        data[0].threadImages[0].path shouldBe "THREAD/105"
        data[0].commentSize shouldBe 3
        data[0].isEdited shouldBe false

        data[2].content shouldBe "" // Response단에서 '' => null로 반환
        data[2].creatorId shouldBe 1
        data[2].creatorImageUrl shouldBe "PROFILE/101"
        data[2].threadImages shouldBe listOf()
        data[2].commentSize shouldBe 0
        data[2].isEdited shouldBe false

        data[3].creatorImageUrl shouldBe null

        size shouldBe 5
        offset shouldBe 0
        totalCount shouldBe 5
        hasNext shouldBe false
    }

    @DisplayName("복수 쓰레드 조회 : 페이지네이션 범위를 벗어난 경우 예외가 발생하지 않는다.")
    @Transactional
    @Test
    fun getThreadsOutOfRangeOK() {
        val result = threadService.getThreads(
            projectId = 1L,
            pageable = PageRequest.of(
                10,
                10,
                Sort.by("id").descending()
            )
        )

        // almost controller response format
        val data = result.content.asReversed()
        val size = result.content.size
        val offset = result.pageable.offset.toInt()
        val totalCount = result.totalElements.toInt()
        val hasNext = result.pageable.offset + result.size < result.totalElements

        data shouldBe listOf()
        size shouldBe 0
        offset shouldBe 100 // page * size
        totalCount shouldBe 5
        hasNext shouldBe false
    }

    @DisplayName("복수 쓰레드 조회 : 존재하지 않는 프로젝트의 쓰레드 조회 시도시 예외가 발생하지 않는다.")
    @Transactional
    @Test
    fun getThreadsProjectNotFoundOK() {
        val result = threadService.getThreads(
            projectId = 99999L,
            pageable = PageRequest.of(
                0,
                10,
                Sort.by("id").descending()
            )
        )

        // almost controller response format
        val data = result.content.asReversed()
        val size = result.content.size
        val offset = result.pageable.offset.toInt()
        val totalCount = result.totalElements.toInt()
        val hasNext = result.pageable.offset + result.size < result.totalElements

        data shouldBe listOf()
        size shouldBe 0
        offset shouldBe 0
        totalCount shouldBe 0
        hasNext shouldBe false
    }

    @DisplayName("쓰레드 상세 조회 : 쓰레드와 댓글 정보들을 상세 조회한다.")
    @Transactional
    @Test
    fun getFullThreadOK() {
        val result = threadService.getFullThread(1L)

        result.id shouldBe 1
        result.content shouldBe "쓰레드 내용 1"
        result.isEdited shouldBe false
        result.creatorId shouldBe 1
        result.creatorNickname shouldBe "사용자 1"
        result.creatorImageUrl shouldBe "PROFILE/101"
        result.threadImages.size shouldBe 3
        result.threadImages[0].path shouldBe "THREAD/105"

        result.comments.size shouldBe 3
        result.comments[0].id shouldBeLessThan result.comments[1].id
        result.comments[0].id shouldBe 1
        result.comments[0].content shouldBe "댓글 내용 1"
        result.comments[0].isEdited shouldBe true
        result.comments[0].commentImages.size shouldBe 3
        result.comments[0].commentImages[0].path shouldBe "COMMENT/109"
        result.comments[1].content shouldBe null
        result.comments[2].commentImages.size shouldBe 0
        result.comments[2].commentImages shouldBe listOf()
    }
}
