package waffle.guam.test

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.transaction.annotation.Transactional
import waffle.guam.Database
import waffle.guam.DatabaseTest
import waffle.guam.db.entity.ImageEntity
import waffle.guam.db.entity.ImageType
import waffle.guam.db.entity.Position
import waffle.guam.db.entity.State
import waffle.guam.db.entity.TaskEntity
import waffle.guam.db.repository.CommentRepository
import waffle.guam.db.repository.ImageRepository
import waffle.guam.db.repository.ProjectRepository
import waffle.guam.db.repository.TaskRepository
import waffle.guam.db.repository.ThreadRepository
import waffle.guam.db.repository.ThreadViewRepository
import waffle.guam.exception.DataNotFoundException
import waffle.guam.exception.InvalidRequestException
import waffle.guam.exception.NotAllowedException
import waffle.guam.model.Image
import waffle.guam.model.ThreadDetail
import waffle.guam.model.ThreadOverView
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
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldNotContainAnyOf
import waffle.guam.db.entity.CommentEntity
import waffle.guam.db.entity.ThreadEntity

@DatabaseTest
class ChatServiceSpec @Autowired constructor(
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

    @DisplayName("복수의 쓰레드 조회 : projectId에 해당하는 프로젝트에 달린 쓰레드들을 조회할 수 있다")
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

    @DisplayName("복수의 쓰레드 조회 : projectId, page, size 정보에 해당되는 쓰레드들의 세부 정보를 조회할 수 있다")
    @Transactional
    @Test
    fun getThreadsEveryDetailOK() {
        val users = database.getUsers()
        val project = database.getProject()
        for (i in 1 until 16) {
            chatService.createThread(
                command = DefaultCommand.CreateThread.copy(userId = users[i % 3].id, content = "Thread Number $i")
            )
        }
        for (i in 0 until 7) {
            chatService.createComment(
                command = DefaultCommand.CreateComment.copy(userId = users[i % 3].id, threadId = 10, content = "filterImages test")
            )
            chatService.createComment(
                command = DefaultCommand.CreateComment.copy(userId = users[i % 3].id, threadId = 11, content = "filterImages test")
            )
        }
        imageRepository.saveAll(
            listOf(
                ImageEntity(parentId = 1, type = ImageType.THREAD),
                ImageEntity(parentId = 1, type = ImageType.THREAD),
                ImageEntity(parentId = 1, type = ImageType.THREAD),
                ImageEntity(parentId = 11, type = ImageType.THREAD),
                ImageEntity(parentId = 11, type = ImageType.THREAD),
                ImageEntity(parentId = 11, type = ImageType.COMMENT),
                ImageEntity(parentId = 11, type = ImageType.COMMENT),
                ImageEntity(parentId = 13, type = ImageType.COMMENT),
                ImageEntity(parentId = 13, type = ImageType.COMMENT),
                ImageEntity(parentId = 13, type = ImageType.COMMENT),
            )
        )
        chatService.editThreadContent(
            EditThreadContent(
                threadId = 13,
                userId = users[13 % 3].id,
                content = "Thread Number 13 that has been edited"
            )
        )
        val result: Page<ThreadOverView> = chatService.getThreads(
            projectId = project.id,
            pageable = PageRequest.of(
                1,
                10,
            )
        )
        val threadImages = imageRepository.findByParentIdAndType(11, ImageType.THREAD)
        val emptyImageList = imageRepository.findByParentIdAndType(13, ImageType.THREAD).map { Image.of(it) }

        result.content[0].id shouldBe 11
        result.content[0].content shouldBe "Thread Number 11"
        result.content[0].isEdited shouldBe false
        result.content[0].commentSize shouldBe 7
        result.content[0].creatorId shouldBe users[11 % 3].id
        result.content[0].creatorImageUrl shouldBe users[11 % 3].image?.getPath()
        result.content[0].threadImages.size shouldBe threadImages.size
        result.content[0].threadImages[0].id shouldBe threadImages[0].id
        result.content[0].threadImages[0].path shouldBe threadImages[0].getPath()

        result.content[2].content shouldBe "Thread Number 13 that has been edited"
        result.content[2].commentSize shouldBe 0
        result.content[2].isEdited shouldBe true
        result.content[2].commentSize shouldBe 0
        result.content[2].creatorId shouldBe users[13 % 3].id
        result.content[2].creatorImageUrl shouldBe users[13 % 3].image?.getPath()
        result.content[2].threadImages.size shouldBe emptyImageList.size
        result.content[2].threadImages shouldBe emptyImageList

        result.content.size shouldBe 5
        result.pageable.offset shouldBe 10
        result.totalElements shouldBe 15
    }

    @DisplayName("복수의 쓰레드 조회 : content 혹은 imageFiles만 존재하는 쓰레드들을 조회해도 예외가 발생하지 않는다.")
    @Transactional
    @Test
    fun getThreadsNoImageOrContentOK() {
        database.getUsers()
        val project = database.getProject()
        chatService.createThread(command = DefaultCommand.CreateThread.copy(content = "Only Content Thread"))

        every {
            imageService.upload(DefaultInput.imageFiles[0], ImageInfo(2L, ImageType.THREAD))
        } returns imageRepository.save(ImageEntity(parentId = 2L, type = ImageType.THREAD))

        chatService.createThread(
            command = DefaultCommand.CreateThread.copy(
                content = null,
                imageFiles = listOf(DefaultInput.imageFiles[0])
            )
        )

        val result: Page<ThreadOverView> = chatService.getThreads(
            projectId = project.id,
            pageable = PageRequest.of(
                0,
                10,
            )
        )
        database.flushAndClear()

        result.content[0].id shouldBe 1
        result.content[0].content shouldBe "Only Content Thread"
        result.content[0].threadImages.size shouldBe 0

        result.content[1].id shouldBe 2
        result.content[1].content shouldBe null
        result.content[1].threadImages.size shouldBe 1
    }

    @DisplayName("복수의 쓰레드 조회 : page와 size에 해당되는 범위에 쓰레드가 없어도 예외는 발생하지 않는다")
    @Transactional
    @Test
    fun getThreadsOutOfRangeOK() {
        val totalThreadNum = 15
        val page = 100
        val size = 100
        database.getUsers()
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

    @DisplayName("복수의 쓰레드 조회 : projectId에 해당하는 프로젝트가 없어도 예외는 발생하지 않는다")
    @Transactional
    @Test
    fun getThreadsProjectNotFoundOK() {
        database.getUsers()
        database.getProject()
        database.getThread()
        val totalThreadNum = 0
        val page = 0
        val size = 10
        val result = chatService.getThreads(
            projectId = 999999,
            pageable = PageRequest.of(page, size)
        )
        result.content.size shouldBe 0
        result.content shouldBe listOf()
        result.pageable.offset shouldBe page * size
        result.totalElements shouldBe totalThreadNum
    }

    @DisplayName("쓰레드 상세 조회 : threadId에 해당하는 쓰레드를 찾아준다")
    @Transactional
    @Test
    fun getFullThreadOK() {
        val threadId = 5L
        val users = database.getUsers()
        database.getProject()
        for (i in 1 until 11) {
            chatService.createThread(
                command = DefaultCommand.CreateThread.copy(userId = users[0].id, content = "Thread Number $i")
            )
        }
        chatService.createComment(
            command = DefaultCommand.CreateComment.copy(threadId = threadId, content = "First", userId = users[1].id)
        )
        chatService.createComment(
            command = DefaultCommand.CreateComment.copy(threadId = threadId, content = "Second", userId = users[2].id)
        )
        val result: ThreadDetail = chatService.getFullThread(threadId)
        result.id shouldBe threadId
        result.creatorId shouldBe users[0].id
        result.creatorImageUrl shouldBe users[0].image?.getPath()
        result.creatorNickname shouldBe users[0].nickname
        result.content shouldBe "Thread Number $threadId"
        result.comments[0].content shouldBe "First"
        result.comments[0].creatorNickname shouldBe users[1].nickname
        result.comments[1].content shouldBe "Second"
        result.comments[1].creatorNickname shouldBe users[2].nickname
    }

    @DisplayName("쓰레드 상세 조회 : threadId에 해당하는 쓰레드와 관련 댓글들의 세부 정보를 조회할 수 있다")
    @Transactional
    @Test
    fun getFullThreadEveryDetailOK() {
        val users = database.getUsers()
        database.getProject()
        chatService.createThread(
            command = DefaultCommand.CreateThread.copy(userId = users[0].id, content = "Thread Number 1")
        )
        for (i in 1 until 16) {
            chatService.createComment(
                command = DefaultCommand.CreateComment.copy(threadId = 1, content = "Comment Number $i", userId = users[i % 3].id)
            )
        }
        imageRepository.saveAll(
            listOf(
                ImageEntity(parentId = 1, type = ImageType.THREAD),
                ImageEntity(parentId = 1, type = ImageType.COMMENT),
                ImageEntity(parentId = 1, type = ImageType.COMMENT),
                ImageEntity(parentId = 1, type = ImageType.COMMENT),
                ImageEntity(parentId = 5, type = ImageType.COMMENT),
                ImageEntity(parentId = 5, type = ImageType.COMMENT),
            )
        )
        chatService.editCommentContent(
            command = DefaultCommand.EditCommentContent.copy(
                commentId = 2,
                userId = users[2 % 3].id,
                content = "Comment Number 2 that has been edited"
            )
        )
        database.flush()

        val threadImages = imageRepository.findByParentIdAndType(1, ImageType.THREAD)
        val commentImages = imageRepository.findByParentIdAndType(1, ImageType.COMMENT)
        val emptyImageList = imageRepository.findByParentIdAndType(2, ImageType.COMMENT).map { Image.of(it) }

        val result: ThreadDetail = chatService.getFullThread(1)

        result.id shouldBe 1
        result.content shouldBe "Thread Number 1"
        result.isEdited shouldBe false
        result.creatorId shouldBe users[0].id
        result.creatorImageUrl shouldBe users[0].image?.getPath()
        result.creatorNickname shouldBe users[0].nickname
        result.threadImages.size shouldBe threadImages.size
        result.threadImages[0].id shouldBe threadImages[0].id
        result.threadImages[0].path shouldBe threadImages[0].getPath()

        result.comments[0].id shouldBe 1
        result.comments[0].content shouldBe "Comment Number 1"
        result.comments[0].isEdited shouldBe false
        result.comments[0].creatorId shouldBe users[1 % 3].id
        result.comments[0].creatorNickname shouldBe users[1 % 3].nickname
        result.comments[0].creatorImageUrl shouldBe users[1 % 3].image?.getPath()
        result.comments[0].commentImages.size shouldBe commentImages.size
        result.comments[0].commentImages[0].id shouldBe commentImages[0].id
        result.comments[0].commentImages[0].path shouldBe commentImages[0].getPath()

        result.comments[1].id shouldBe 2
        result.comments[1].content shouldBe "Comment Number 2 that has been edited"
        result.comments[1].isEdited shouldBe true
        result.comments[1].creatorNickname shouldBe users[2 % 3].nickname
        result.comments[1].creatorImageUrl shouldBe users[2 % 3].image?.getPath()
        result.comments[1].commentImages.size shouldBe emptyImageList.size
        result.comments[1].commentImages shouldBe emptyImageList
    }

    @DisplayName("쓰레드 상세 조회 : content 혹은 imageFiles만 존재하는 쓰레드와 댓글들을 조회해도 예외가 발생하지 않는다.")
    @Transactional
    @Test
    fun getFullThreadNoImageOrContentOK() {
        database.getUsers()
        val project = database.getProject()
        chatService.createThread(command = DefaultCommand.CreateThread.copy(content = "Only Content Thread"))
        chatService.createComment(command = DefaultCommand.CreateComment.copy(threadId = 1, content = "Only Content Comment"))

        every {
            imageService.upload(DefaultInput.imageFiles[0], ImageInfo(2L, ImageType.THREAD))
        } returns imageRepository.save(ImageEntity(parentId = 2L, type = ImageType.THREAD))
        every {
            imageService.upload(DefaultInput.imageFiles[1], ImageInfo(2L, ImageType.COMMENT))
        } returns imageRepository.save(ImageEntity(parentId = 2L, type = ImageType.COMMENT))

        chatService.createThread(
            command = DefaultCommand.CreateThread.copy(
                content = null,
                imageFiles = listOf(DefaultInput.imageFiles[0])
            )
        )
        chatService.createComment(
            command = DefaultCommand.CreateComment.copy(
                threadId = 2,
                content = null,
                imageFiles = listOf(DefaultInput.imageFiles[1])
            )
        )

        database.flushAndClear()

        val noImageResult: ThreadDetail = chatService.getFullThread(1)
        val noContentResult: ThreadDetail = chatService.getFullThread(2)

        noImageResult.id shouldBe 1
        noImageResult.content shouldBe "Only Content Thread"
        noImageResult.threadImages.size shouldBe 0
        noImageResult.comments[0].content shouldBe "Only Content Comment"
        noImageResult.comments[0].commentImages.size shouldBe 0

        noContentResult.id shouldBe 2
        noContentResult.content shouldBe null
        noContentResult.threadImages.size shouldBe 1
        noContentResult.comments[0].content shouldBe null
        noContentResult.comments[0].commentImages.size shouldBe 1
    }

    @DisplayName("쓰레드 상세 조회 : threadId에 해당하는 쓰레드가 없다면 예외가 발생한다")
    @Transactional
    @Test
    fun getFullThreadThreadNotFoundException() {
        shouldThrowExactly<RuntimeException> {
            chatService.getFullThread(99999999999)
        }
    }

    @DisplayName("공지 쓰레드 설정 : projectId에 해당하는 프로젝트의 멤버는 공지 쓰레드로 threadId에 해당하는 쓰레드를 설정할 수 있다.")
    @Transactional
    @Test
    fun setNoticeThreadOK() {
        val user = database.getUser()
        val prevProject = database.getProject().copy()
        taskRepository.save(DefaultInput.task.copy())
        val thread = database.getThread()

        val result = chatService.setNoticeThread(
            DefaultCommand.SetNoticeThread.copy(
                projectId = prevProject.id,
                threadId = thread.id,
                userId = user.id
            )
        )
        val updatedProject = projectRepository.findById(prevProject.id).get()
        result shouldBe true
        updatedProject.id shouldBe prevProject.id
        prevProject.noticeThreadId shouldBe null
        updatedProject.noticeThreadId shouldBe thread.id
    }

    @DisplayName("공지 쓰레드 설정 : 존재하지 않는 프로젝트에 공지 쓰레드를 설정하려고 하면 예외가 발생한다")
    @Transactional
    @Test
    fun setNoticeThreadProjectNotFoundException() {
        database.getProject()
        shouldThrowExactly<DataNotFoundException> {
            chatService.setNoticeThread(
                command = DefaultCommand.SetNoticeThread.copy(projectId = 9999999)
            )
        }
    }

    @DisplayName("공지 쓰레드 설정 : 프로젝트와 연관이 없는 사용자(=task가 없는 경우)가 쓰레드를 설정하려고 하면 예외가 발생한다")
    @Transactional
    @Test
    fun setNoticeThreadTaskNotFoundException() {
        val users = database.getUsers()
        val project = database.getProject()
        taskRepository.saveAll(
            listOf(
                DefaultInput.task.copy(projectId = project.id, userId = users[0].id, state = State.LEADER),
                DefaultInput.task.copy(projectId = project.id, userId = users[1].id, state = State.MEMBER),
            )
        )
        shouldThrowExactly<NotAllowedException> {
            chatService.setNoticeThread(
                command = DefaultCommand.SetNoticeThread.copy(
                    projectId = project.id,
                    userId = users[2].id
                )
            )
        }
    }

    @DisplayName("공지 쓰레드 설정 : GUEST 상태의 사용자가 쓰레드를 설정하려고 하면 예외가 발생한다")
    @Transactional
    @Test
    fun setNoticeThreadGuestTaskNotAllowedException() {
        val users = database.getUsers()
        val project = database.getProject()
        taskRepository.saveAll(
            listOf(
                DefaultInput.task.copy(projectId = project.id, userId = users[0].id, state = State.LEADER),
                DefaultInput.task.copy(projectId = project.id, userId = users[1].id, state = State.MEMBER),
                DefaultInput.task.copy(projectId = project.id, userId = users[2].id, state = State.GUEST),
            )
        )
        shouldThrowExactly<NotAllowedException> {
            chatService.setNoticeThread(
                command = DefaultCommand.SetNoticeThread.copy(
                    projectId = project.id,
                    userId = users[2].id
                )
            )
        }
    }

    @DisplayName("공지 쓰레드 설정 : threadId에 해당하는 쓰레드가 존재하지 않을 때 해당 쓰레드를 공지 쓰레드로 설정하려고 하면 예외가 발생한다")
    @Transactional
    @Test
    fun setNoticeThreadThreadNotFoundException() {
        val users = database.getUsers()
        val project = database.getProject()
        taskRepository.saveAll(
            listOf(
                DefaultInput.task.copy(projectId = project.id, userId = users[0].id, state = State.LEADER),
                DefaultInput.task.copy(projectId = project.id, userId = users[1].id, state = State.MEMBER),
                DefaultInput.task.copy(projectId = project.id, userId = users[2].id, state = State.GUEST),
            )
        )
        database.flush()
        shouldThrowExactly<DataNotFoundException> {
            chatService.setNoticeThread(
                command = DefaultCommand.SetNoticeThread.copy(
                    projectId = project.id,
                    threadId = 9999999999999999,
                    userId = users[1].id
                )
            )
        }
    }

    @DisplayName("공지 쓰레드 제거 : projectId에 해당하는 프로젝트의 멤버는 공지 쓰레드를 삭제할 수 있다.")
    @Transactional
    @Test
    fun removeNoticeThreadOK() {
        val user = database.getUser()
        val prevProject = database.getProject().copy()
        taskRepository.save(DefaultInput.task.copy())
        val thread = database.getThread()

        chatService.setNoticeThread(
            DefaultCommand.SetNoticeThread.copy(
                projectId = prevProject.id,
                threadId = thread.id,
                userId = user.id
            )
        )
        val prevProject2 = projectRepository.findById(prevProject.id).get().copy()
        val result = chatService.removeNoticeThread(
            DefaultCommand.RemoveNoticeThread.copy(
                projectId = prevProject2.id,
                userId = user.id
            )
        )
        val updatedProject = projectRepository.findById(prevProject2.id).get()
        result shouldBe true
        prevProject.id shouldBe prevProject2.id
        prevProject2.id shouldBe updatedProject.id
        prevProject.noticeThreadId shouldBe null
        prevProject2.noticeThreadId shouldBe thread.id
        updatedProject.noticeThreadId shouldBe null
    }

    @DisplayName("공지 쓰레드 제거 : 존재하지 않는 프로젝트에 공지 쓰레드를 삭제하려고 하면 예외가 발생한다")
    @Transactional
    @Test
    fun removeNoticeThreadProjectNotFoundException() {
        database.getProject()
        shouldThrowExactly<DataNotFoundException> {
            chatService.removeNoticeThread(
                command = DefaultCommand.RemoveNoticeThread.copy(projectId = 9999999)
            )
        }
    }

    @DisplayName("공지 쓰레드 제거 : 프로젝트와 연관이 없는 사용자(=task가 없는 경우)가 쓰레드를 삭제하려고 하면 예외가 발생한다")
    @Transactional
    @Test
    fun removeNoticeThreadTaskNotFoundException() {
        val users = database.getUsers()
        val project = database.getProject()
        taskRepository.saveAll(
            listOf(
                DefaultInput.task.copy(projectId = project.id, userId = users[0].id, state = State.LEADER),
                DefaultInput.task.copy(projectId = project.id, userId = users[1].id, state = State.MEMBER),
            )
        )
        shouldThrowExactly<NotAllowedException> {
            chatService.removeNoticeThread(
                command = DefaultCommand.RemoveNoticeThread.copy(
                    projectId = project.id,
                    userId = users[2].id
                )
            )
        }
    }

    @DisplayName("공지 쓰레드 제거 : GUEST 상태의 사용자가 쓰레드를 삭제하려고 하면 예외가 발생한다")
    @Transactional
    @Test
    fun removeNoticeThreadGuestTaskNotAllowedException() {
        val users = database.getUsers()
        val project = database.getProject()
        taskRepository.saveAll(
            listOf(
                DefaultInput.task.copy(projectId = project.id, userId = users[0].id, state = State.LEADER),
                DefaultInput.task.copy(projectId = project.id, userId = users[1].id, state = State.MEMBER),
                DefaultInput.task.copy(projectId = project.id, userId = users[2].id, state = State.GUEST),
            )
        )
        shouldThrowExactly<NotAllowedException> {
            chatService.removeNoticeThread(
                command = DefaultCommand.RemoveNoticeThread.copy(
                    projectId = project.id,
                    userId = users[2].id
                )
            )
        }
    }

    @DisplayName("쓰레드 생성 : projectId에 해당하는 프로젝트에 content 정보로 쓰레드를 생성한다")
    @Transactional
    @Test
    fun createThreadWithContentOK() {
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

    @DisplayName("쓰레드 생성 : projectId에 해당하는 프로젝트에 imageFiles 배열 정보로 쓰레드를 생성한다")
    @Transactional
    @Test
    fun createThreadWithImagesOK() {
        val targetThreadId = 2L
        val user = database.getUser()
        val project = database.getProject()

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
                    content = "Content for Comment $i",
                    imageFiles = DefaultInput.imageFiles
                )
            )
        }
        val createdThread = threadRepository.findById(targetThreadId).get()
        val createdImages = database.getImages()

        createdThread.id shouldBe targetThreadId
        createdThread.projectId shouldBe project.id
        createdThread.userId shouldBe user.id
        createdThread.content shouldBe "Content for Comment 3"
        createdImages.size shouldBe 9
        createdImages[6].id shouldBe 7
        createdImages[6].type shouldBe ImageType.THREAD
        createdImages[6].parentId shouldBe targetThreadId
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
        val thread = threadRepository.save(ThreadEntity(
            projectId = project.id,
            userId = user.id,
            content = null,
        ))
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
        val thread = threadRepository.save(ThreadEntity(
            projectId = project.id,
            userId = user.id,
            content = null,
        ))
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

    @DisplayName("댓글 생성 : threadId에 해당하는 쓰레드에 content 정보로 댓글을 생성한다.")
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

    @DisplayName("댓글 생성 : projectId에 해당하는 프로젝트에 imageFiles 배열 정보로 댓글을 생성한다")
    @Transactional
    @Test
    fun createCommentWithImagesOK() {
        val targetCommentId = 2L
        val user = database.getUser()
        database.getProject()
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

    @DisplayName("댓글 수정 : commentId에 해당하는 댓글의 작성자는 댓글을 수정할 수 있다.")
    @Transactional
    @Test
    fun editCommentContentOK() {
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
        database.flush()
        val editedComment = commentRepository.findById(1).get()

        result shouldBe true
        createdComment.content shouldBe "New Comment"
        editedComment.content shouldBe "edited Content"
        editedComment.createdAt shouldNotBe editedComment.modifiedAt
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
        val comment = commentRepository.save(CommentEntity(
            threadId = thread.id,
            userId = user.id,
            content = null,
        ))
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
            state = State.MEMBER
        )
        val imageFiles = listOf(
            MockMultipartFile("파일1", "기존 파일명1.png", MediaType.IMAGE_PNG_VALUE, "파일 1 내용".toByteArray()),
            MockMultipartFile("파일2", "기존 파일명2.png", MediaType.IMAGE_PNG_VALUE, "파일 2 내용".toByteArray()),
            MockMultipartFile("파일3", "기존 파일명3.png", MediaType.IMAGE_PNG_VALUE, "파일 3 내용".toByteArray())
        )
    }
}
