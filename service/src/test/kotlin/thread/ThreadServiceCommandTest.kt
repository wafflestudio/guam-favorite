package waffle.guam.thread

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import waffle.guam.DataNotFoundException
import waffle.guam.InvalidRequestException
import waffle.guam.NotAllowedException
import waffle.guam.annotation.DatabaseTest
import waffle.guam.comment.CommentRepository
import waffle.guam.project.ProjectRepository
import waffle.guam.task.TaskService
import waffle.guam.thread.command.CreateJoinThread
import waffle.guam.thread.command.CreateThread
import waffle.guam.thread.command.DeleteThread
import waffle.guam.thread.command.EditThreadContent
import waffle.guam.thread.command.SetNoticeThread
import java.util.Optional
import javax.persistence.EntityManager

@DatabaseTest(["thread/image.sql", "thread/user.sql", "thread/project.sql", "thread/task.sql", "thread/thread.sql", "thread/comment.sql"])
class ThreadServiceCommandTest @Autowired constructor(
    private val entityManager: EntityManager,
    private val projectRepository: ProjectRepository,
    private val threadRepository: ThreadRepository,
    threadViewRepository: ThreadViewRepository,
    taskService: TaskService,
    commentRepository: CommentRepository,
) {

    private val threadService = ThreadServiceImpl(
        threadRepository,
        threadViewRepository,
        projectRepository,
        taskService,
        commentRepository
    )

    @DisplayName("공지 쓰레드 설정 : 리더와 멤버는 특정 쓰레드를 공지로 지정할 수 있다.")
    @Transactional
    @Test
    fun setNoticeThreadOK() {
        val command = SetNoticeThread(
            projectId = 1,
            threadId = 2,
            userId = 1,
        )

        val event = threadService.setNoticeThread(command)

        event.projectId shouldBe command.projectId
        event.threadId shouldBe command.threadId

        val project = projectRepository.findById(command.projectId).get()

        project.noticeThreadId shouldBe command.threadId
    }

    @DisplayName("공지 쓰레드 설정 : 리더와 멤버는 프로젝트의 기존 공지 쓰레드를 제거할 수 있다.")
    @Transactional
    @Test
    fun setNoticeThreadRemoveOK() {
        val command = SetNoticeThread(
            projectId = 2,
            threadId = null,
            userId = 1,
        )

        val event = threadService.setNoticeThread(command)

        event.projectId shouldBe command.projectId
        event.threadId shouldBe null

        val project = projectRepository.findById(command.projectId).get()

        project.noticeThreadId shouldBe null
    }

    @DisplayName("공지 쓰레드 설정 예외 : 존재하지 않는 프로젝트에 공지 쓰레드 설정 시도시 예외가 발생한다.")
    @Transactional
    @Test
    fun setNoticeThreadProjectNotFoundException() {
        shouldThrowExactly<DataNotFoundException> {
            threadService.setNoticeThread(
                command = SetNoticeThread(
                    projectId = 999999,
                    threadId = 1,
                    userId = 2,
                )
            )
        }
    }

    @DisplayName("공지 쓰레드 설정 예외 : 게스트 등 비멤버들은 공지 쓰레드 설정 시도시 예외가 발생한다.")
    @Transactional
    @Test
    fun setNoticeThreadGuestNotAllowedException() {
        shouldThrowExactly<NotAllowedException> {
            threadService.setNoticeThread(
                command = SetNoticeThread(
                    projectId = 1,
                    threadId = 2,
                    userId = 2,
                )
            )
        }
    }

    // 주의: mocking 없이 MockMultipartFile 리스트 대입하면 S3에 그대로 업로드됨
    @DisplayName("쓰레드 생성 : 리더와 멤버는 새로운 쓰레드를 생성할 수 있다.")
    @Transactional
    @Test
    fun createThreadOK() {
        val command = CreateThread(
            projectId = 1,
            userId = 1,
            content = "새로운 쓰레드",
            imageFiles = null
        )

        val event = threadService.createThread(command)

        event.threadId shouldBeGreaterThan 6
        event.creatorId shouldBe command.userId
        event.creatorName shouldBe "사용자 1"
        event.content shouldBe command.content
        event.imageFiles shouldBe null

        val createdThread = threadService.getFullThread(event.threadId)

        createdThread.id shouldBe event.threadId
        createdThread.content shouldBe command.content
        createdThread.creatorId shouldBe command.userId
        createdThread.isEdited shouldBe false
        createdThread.threadImages shouldBe listOf()
        createdThread.comments.size shouldBe 0
    }

    @DisplayName("쓰레드 생성 예외 : 존재하지 않는 프로젝트에 쓰레드 생성 시도시 예외가 발생한다.")
    @Transactional
    @Test
    fun createThreadProjectNotFoundException() {
        shouldThrowExactly<DataNotFoundException> {
            threadService.createThread(
                command = CreateThread(
                    projectId = 999999999999,
                    userId = 1,
                    content = "새로운 쓰레드",
                    imageFiles = null
                )
            )
        }
    }

    @DisplayName("쓰레드 생성 예외 : 게스트 등 비멤버들은 일반 쓰레드 생성 시도시 예외가 발생한다.")
    @Transactional
    @Test
    fun createThreadGuestNotAllowedException() {
        shouldThrowExactly<NotAllowedException> {
            threadService.createThread(
                command = CreateThread(
                    projectId = 1,
                    userId = 2,
                    content = "조인 쓰레드 이외의 쓰레드를 게스트가 달려고 시도한 경우",
                    imageFiles = null
                )
            )
        }
    }

    @DisplayName("조인 쓰레드 생성 : 리더와 멤버는 새로운 쓰레드를 생성할 수 있다.")
    @Transactional
    @Test
    fun createJoinRequestThreadOK() {
        val command = CreateJoinThread(
            projectId = 1,
            userId = 1,
            content = "참가 신청 쓰레드"
        )

        val event = threadService.createJoinThread(command)

        event.threadId shouldBeGreaterThan 6

        val createdJoinThread = threadService.getFullThread(event.threadId)

        createdJoinThread.id shouldBe event.threadId
        createdJoinThread.content shouldBe command.content
        createdJoinThread.creatorId shouldBe command.userId
        createdJoinThread.isEdited shouldBe false
        createdJoinThread.threadImages shouldBe listOf()
        createdJoinThread.comments.size shouldBe 0
    }

    @DisplayName("쓰레드 수정 : 쓰레드의 생성자는 해당 쓰레드를 수정할 수 있다.")
    @Transactional
    @Test
    fun editThreadContentOK() {
        val command = EditThreadContent(
            threadId = 1,
            userId = 1,
            content = "수정된 쓰레드 내용"
        )

        val event = threadService.editThreadContent(command)

        event.threadId shouldBe command.threadId

        entityManager.flush()
        val editedThread = threadService.getFullThread(command.threadId)

        editedThread.id shouldBe event.threadId
        editedThread.content shouldBe command.content
        editedThread.creatorId shouldBe command.userId
        editedThread.isEdited shouldBe true
        editedThread.threadImages.size shouldBe 3
        editedThread.threadImages[0].path shouldBe "THREAD/105"
        editedThread.comments.size shouldBe 3
        editedThread.comments[0].content shouldBe "댓글 내용 1"
        editedThread.comments[0].commentImages.size shouldBe 3
        editedThread.comments[0].commentImages[0].path shouldBe "COMMENT/109"
    }

    @DisplayName("쓰레드 수정 예외 : 타인이 생성한 쓰레드를 수정 시도시 예외가 발생한다.")
    @Transactional
    @Test
    fun editThreadContentEditOthersThreadNotAllowedException() {
        shouldThrowExactly<NotAllowedException> {
            threadService.editThreadContent(
                command = EditThreadContent(
                    threadId = 4,
                    userId = 1,
                    content = "타인이 작성한 쓰레드 수정 시도"
                )
            )
        }
    }

    @DisplayName("쓰레드 수정 예외 : 이전과 동일한 내용으로 쓰레드를 수정 시도시 예외가 발생한다.")
    @Transactional
    @Test
    fun editThreadContentSameContentException() {
        shouldThrowExactly<InvalidRequestException> {
            threadService.editThreadContent(
                command = EditThreadContent(
                    threadId = 1,
                    userId = 1,
                    content = "쓰레드 내용 1"
                )
            )
        }
    }

    @DisplayName("쓰레드 삭제 : 쓰레드의 생성자는 해당 쓰레드를 삭제할 수 있다.")
    @Transactional
    @Test
    fun deleteThreadOK() {
        val command = DeleteThread(
            threadId = 2,
            userId = 1,
        )

        val event = threadService.deleteThread(command)

        event.threadId shouldBe command.threadId
        event.projectId shouldBe 1

        entityManager.flush()
        val deletedThread = threadRepository.findById(command.threadId)

        deletedThread shouldBe Optional.empty()
    }

    @DisplayName("쓰레드 삭제 : 아직 댓글이 달린 쓰레드는 삭제되지는 않고 내용과 이미지들만 삭제된다.")
    @Transactional
    @Test
    fun deleteThreadOnlyEmptiesThreadWithCommentsOK() {
        val command = DeleteThread(
            threadId = 5,
            userId = 3,
        )
        val prevThread = threadService.getFullThread(command.threadId)

        val event = threadService.deleteThread(command)
        entityManager.flush()
        entityManager.clear()

        event.threadId shouldBe command.threadId
        event.projectId shouldBe 1

        val deletedThread = threadService.getFullThread(command.threadId)

        prevThread.id shouldBe event.threadId
        prevThread.content shouldBe "삭제되기 위해 존재하는 쓰레드 1"
        prevThread.creatorId shouldBe command.userId
        prevThread.isEdited shouldBe false
        prevThread.threadImages.size shouldBe 1
        prevThread.threadImages[0].path shouldBe "THREAD/113"
        prevThread.comments.size shouldBe 1
        prevThread.comments[0].content shouldBe "삭제될 쓰레드에 달려있을 댓글"
        prevThread.comments[0].commentImages.size shouldBe 1
        prevThread.comments[0].commentImages[0].path shouldBe "COMMENT/114"

        deletedThread.id shouldBe event.threadId
        deletedThread.content shouldBe "" // 컨트롤러에서 반환할 때만 ""
        deletedThread.creatorId shouldBe command.userId
        deletedThread.isEdited shouldBe true
        // deletedThread.threadImages : EventListener에 의해 제거됨
        deletedThread.comments.size shouldBe 1
        deletedThread.comments[0].content shouldBe "삭제될 쓰레드에 달려있을 댓글"
        deletedThread.comments[0].commentImages.size shouldBe 1
        deletedThread.comments[0].commentImages[0].path shouldBe "COMMENT/114"
    }

    @DisplayName("쓰레드 삭제 예외 : 타인이 생성한 쓰레드를 삭제 시도시 예외가 발생한다.")
    @Transactional
    @Test
    fun deleteThreadDeleteOthersThreadNotAllowedException() {
        shouldThrowExactly<NotAllowedException> {
            threadService.deleteThread(
                command = DeleteThread(
                    threadId = 4,
                    userId = 1,
                )
            )
        }
    }

    @DisplayName("쓰레드 삭제 예외 : 게스트 등 비멤버들은 자신이 생성한 쓰레드를 삭제 시도시 예외가 발생한다.")
    @Transactional
    @Test
    fun deleteThreadDeleteGuestNotAllowedException() {
        shouldThrowExactly<NotAllowedException> {
            threadService.deleteThread(
                command = DeleteThread(
                    threadId = 4,
                    userId = 2,
                )
            )
        }
    }
}
