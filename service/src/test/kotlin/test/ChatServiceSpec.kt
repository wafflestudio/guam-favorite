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
import waffle.guam.DefaultDataInfo
import waffle.guam.db.entity.ImageType
import waffle.guam.db.entity.ImageEntity
import waffle.guam.db.entity.State
import waffle.guam.db.repository.CommentRepository
import waffle.guam.db.repository.ProjectRepository
import waffle.guam.db.repository.ThreadRepository
import waffle.guam.db.repository.ThreadViewRepository
import waffle.guam.db.repository.TaskRepository
import waffle.guam.db.repository.ImageRepository
import waffle.guam.exception.DataNotFoundException
import waffle.guam.exception.InvalidRequestException
import waffle.guam.exception.NotAllowedException
import waffle.guam.model.Image
import waffle.guam.model.ThreadDetail
import waffle.guam.model.ThreadOverView
import waffle.guam.service.ChatService
import waffle.guam.service.ImageService
import waffle.guam.service.command.SetNoticeThread
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
    private val taskRepository: TaskRepository,
    private val imageRepository: ImageRepository,

//    @MockK
    private val imageService: ImageService,

    private val database: Database
) {
    private val chatService = ChatService(
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
//        MockKAnnotations.init(this)
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

    @DisplayName("projectId, page, size 정보에 해당되는 쓰레드들의 세부 정보를 조회할 수 있다")
    @Transactional
    @Test
    fun getThreadsEveryDetailOK() {
        val users = database.getUsers()
        val project = database.getProject()
        for (i in 1 until 16) {
            chatService.createThread(
                command = DefaultCommand.CreateThread.copy(userId= users[i%3].id, content = "Thread Number $i")
            )
        }
        for (i in 0 until 7) {
            chatService.createComment(
                command = DefaultCommand.CreateComment.copy(userId= users[i%3].id, threadId = 10, content = "filterImages test")
            )
            chatService.createComment(
                command = DefaultCommand.CreateComment.copy(userId= users[i%3].id, threadId = 11, content = "filterImages test")
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
        chatService.editThreadContent(EditThreadContent(
            threadId= 13,
            userId= users[13%3].id,
            content = "Thread Number 13 that has been edited"
        ))
        val result: Page<ThreadOverView> = chatService.getThreads(
            projectId = project.id,
            pageable = PageRequest.of(
                1,
                10,
            )
        )
        val threadImages = imageRepository.findByParentIdAndType(11, ImageType.THREAD)
        val emptyImageList = imageRepository.findByParentIdAndType(13, ImageType.THREAD).map { Image.of(it)}

        result.content[0].id shouldBe 11
        result.content[0].content shouldBe "Thread Number 11"
        result.content[0].isEdited shouldBe false
        result.content[0].commentSize shouldBe 7
        result.content[0].creatorId shouldBe users[11%3].id
        result.content[0].creatorImageUrl shouldBe users[11%3].image?.path
        result.content[0].threadImages.size shouldBe threadImages.size
        result.content[0].threadImages[0].id shouldBe threadImages[0].id
        result.content[0].threadImages[0].path shouldBe threadImages[0].path

        result.content[2].content shouldBe "Thread Number 13 that has been edited"
        result.content[2].commentSize shouldBe 0
        result.content[2].isEdited shouldBe true
        result.content[2].commentSize shouldBe 0
        result.content[2].creatorId shouldBe users[13%3].id
        result.content[2].creatorImageUrl shouldBe users[13%3].image?.path
        result.content[2].threadImages.size shouldBe emptyImageList.size
        result.content[2].threadImages shouldBe emptyImageList

        result.content.size shouldBe 5
        result.pageable.offset shouldBe 10
        result.totalElements shouldBe 15
    }

    @DisplayName("page와 size에 해당되는 범위에 쓰레드가 없어도 예외는 발생하지 않는다")
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

    @DisplayName("projectId에 해당하는 프로젝트가 없어도 예외는 발생하지 않는다")
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

    @DisplayName("threadId에 해당하는 쓰레드를 찾아준다")
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
        result.creatorImageUrl shouldBe users[0].image?.path
        result.creatorNickname shouldBe users[0].nickname
        result.content shouldBe "Thread Number $threadId"
        result.comments[0].content shouldBe "First"
        result.comments[0].creatorNickname shouldBe users[1].nickname
        result.comments[1].content shouldBe "Second"
        result.comments[1].creatorNickname shouldBe users[2].nickname
    }

    @DisplayName("threadId에 해당하는 쓰레드와 관련 댓글들의 세부 정보를 조회할 수 있다")
    @Transactional
    @Test
    fun getFullThreadEveryDetailOK() {
        val users = database.getUsers()
        database.getProject()
        chatService.createThread(
            command = DefaultCommand.CreateThread.copy(userId= users[0].id, content = "Thread Number 1")
        )
        for (i in 1 until 16) {
            chatService.createComment(
                command = DefaultCommand.CreateComment.copy(threadId = 1, content = "Comment Number $i", userId = users[i%3].id)
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
                commentId= 2,
                userId= users[2%3].id,
                content = "Comment Number 2 that has been edited"
            ))
        database.flush()

        val threadImages = imageRepository.findByParentIdAndType(1, ImageType.THREAD)
        val commentImages = imageRepository.findByParentIdAndType(1, ImageType.COMMENT)
        val emptyImageList = imageRepository.findByParentIdAndType(2, ImageType.COMMENT).map { Image.of(it)}

        val result: ThreadDetail = chatService.getFullThread(1)

        result.id shouldBe 1
        result.content shouldBe "Thread Number 1"
        result.isEdited shouldBe false
        result.creatorId shouldBe users[0].id
        result.creatorImageUrl shouldBe users[0].image?.path
        result.creatorNickname shouldBe users[0].nickname
        result.threadImages.size shouldBe threadImages.size
        result.threadImages[0].id shouldBe threadImages[0].id
        result.threadImages[0].path shouldBe threadImages[0].path

        result.comments[0].id shouldBe 1
        result.comments[0].content shouldBe "Comment Number 1"
        result.comments[0].isEdited shouldBe false
        result.comments[0].creatorId shouldBe users[1%3].id
        result.comments[0].creatorNickname shouldBe users[1%3].nickname
        result.comments[0].creatorImageUrl shouldBe users[1%3].image?.path
        result.comments[0].commentImages.size shouldBe commentImages.size
        result.comments[0].commentImages[0].id shouldBe commentImages[0].id
        result.comments[0].commentImages[0].path shouldBe commentImages[0].path

        result.comments[1].id shouldBe 2
        result.comments[1].content shouldBe "Comment Number 2 that has been edited"
        result.comments[1].isEdited shouldBe true
        result.comments[1].creatorNickname shouldBe users[2%3].nickname
        result.comments[1].creatorImageUrl shouldBe users[2%3].image?.path
        result.comments[1].commentImages.size shouldBe emptyImageList.size
        result.comments[1].commentImages shouldBe emptyImageList
    }

    @DisplayName("threadId에 해당하는 쓰레드가 없다면 예외가 발생한다")
    @Transactional
    @Test
    fun getFullThreadThreadNotFoundException() {
        shouldThrowExactly<RuntimeException> {
            chatService.getFullThread(99999999999)
        }
    }

    @DisplayName("projectId에 해당하는 프로젝트의 멤버는 공지 쓰레드로 threadId에 해당하는 쓰레드를 설정할 수 있다.")
    @Transactional
    @Test
    fun setNoticeThreadOK() {
        val user = database.getUser()
        val prevProject = database.getProject().copy()
        database.getTask()
        val thread = database.getThread()

        val result = chatService.setNoticeThread(DefaultCommand.SetNoticeThread.copy(
            projectId = prevProject.id,
            threadId = thread.id,
            userId = user.id
        ))

        val updatedProject = database.getProject()

        result shouldBe true
        updatedProject.id shouldBe prevProject.id
        prevProject.noticeThreadId shouldBe null
        updatedProject.noticeThreadId shouldBe thread.id
    }

    @DisplayName("projectId에 해당되는 프로젝트에 공지 쓰레드를 설정하려고 하면 예외가 발생한다")
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

    @DisplayName("projectId와 userId에 해당되는 task가 없을 때 쓰레드를 설정하려고 하면 예외가 발생한다")
    @Transactional
    @Test
    fun setNoticeThreadTaskNotFoundException() {
        val users = database.getUsers()
        val project = database.getProject()
        taskRepository.saveAll(listOf(
            DefaultDataInfo.task.copy(projectId = project.id, userId = users[0].id, state = State.LEADER),
            DefaultDataInfo.task.copy(projectId = project.id, userId = users[1].id, state = State.MEMBER),
        ))
        shouldThrowExactly<NotAllowedException> {
            chatService.setNoticeThread(
                command = DefaultCommand.SetNoticeThread.copy(
                    projectId = project.id,
                    userId = users[2].id
                )
            )
        }
    }

    @DisplayName("projectId와 userId에 해당되는 task의 State가 GUEST일 때 쓰레드를 설정하려고 하면 예외가 발생한다")
    @Transactional
    @Test
    fun setNoticeThreadGuestTaskNotAllowedException() {
        val users = database.getUsers()
        val project = database.getProject()
        taskRepository.saveAll(listOf(
            DefaultDataInfo.task.copy(projectId = project.id, userId = users[0].id, state = State.LEADER),
            DefaultDataInfo.task.copy(projectId = project.id, userId = users[1].id, state = State.MEMBER),
            DefaultDataInfo.task.copy(projectId = project.id, userId = users[2].id, state = State.GUEST),
        ))
        shouldThrowExactly<NotAllowedException> {
            chatService.setNoticeThread(
                command = DefaultCommand.SetNoticeThread.copy(
                    projectId = project.id,
                    userId = users[2].id
                )
            )
        }
    }

    @DisplayName("threadId에 해당하는 쓰레드가 존재하지 않을 때 해당 쓰레드를 공지 쓰레드로 설정하려고 하면 예외가 발생한다")
    @Transactional
    @Test
    fun setNoticeThreadThreadNotFoundException() {
        val users = database.getUsers()
        val project = database.getProject()
        taskRepository.saveAll(listOf(
            DefaultDataInfo.task.copy(projectId = project.id, userId = users[0].id, state = State.LEADER),
            DefaultDataInfo.task.copy(projectId = project.id, userId = users[1].id, state = State.MEMBER),
            DefaultDataInfo.task.copy(projectId = project.id, userId = users[2].id, state = State.GUEST),
        ))
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

    @DisplayName("projectId에 해당하는 프로젝트에 content 정보로 쓰레드를 생성한다")
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

// TODO(createThreadWithImagesOK)
//    @DisplayName("projectId에 해당하는 프로젝트에 imageUrl 배열 정보로 쓰레드를 생성한다")
//    @Transactional
//    @Test
//    fun createThreadWithImagesOK() {
//    }

// TODO(createThreadWithContentAndImagesOK)
//    @DisplayName("projectId에 해당하는 프로젝트에 content와 imageUrl 배열 정보로 쓰레드를 생성한다")
//    @Transactional
//    @Test
//    fun createThreadWithContentAndImagesOK() {
//    }

    @DisplayName("쓰레드 생성시 content와 imageUrl 배열 정보가 null이라면 예외가 발생한다")
    @Transactional
    @Test
    fun createThreadNullInputException() {
        shouldThrowExactly<InvalidRequestException> {
            chatService.createThread(
                command = DefaultCommand.CreateThread.copy(content = null, imageFiles = null)
            )
        }
    }

    @DisplayName("쓰레드 생성시 content와 imageUrl 배열 정보가 비어있다면 예외가 발생한다")
    @Transactional
    @Test
    fun createThreadEmptyNoInputException() {
        shouldThrowExactly<InvalidRequestException> {
            chatService.createThread(
                command = DefaultCommand.CreateThread.copy(content = "", imageFiles = listOf())
            )
        }
    }

    @DisplayName("projectId에 해당하는 프로젝트가 없다면 예외가 발생한다")
    @Transactional
    @Test
    fun createThreadThreadNotFoundException() {
        shouldThrowExactly<DataNotFoundException> {
            chatService.createThread(
                command = DefaultCommand.CreateThread.copy(projectId = 9999999)
            )
        }
    }

    @DisplayName("threadId에 해당하는 쓰레드의 작성자는 쓰레드를 수정할 수 있다.")
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
        shouldThrowExactly<NotAllowedException> {
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
        shouldThrowExactly<NotAllowedException> {
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

// TODO(createCommentWithImagesOK)
//    @DisplayName("projectId에 해당하는 프로젝트에 imageUrl 배열 정보로 댓글을 생성한다")
//    @Transactional
//    @Test
//    fun createThreadWithImagesOK() {
//    }

// TODO(createCommentWithContentAndImagesOK)
//    @DisplayName("projectId에 해당하는 프로젝트에 content와 imageUrl 배열 정보로 댓글을 생성한다")
//    @Transactional
//    @Test
//    fun createThreadWithContentAndImagesOK() {
//    }

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
        shouldThrowExactly<NotAllowedException> {
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
        shouldThrowExactly<NotAllowedException> {
            chatService.deleteComment(
                command = DefaultCommand.DeleteComment.copy(
                    userId = 999999999999999,
                )
            )
        }
    }
}

object DefaultCommand {
    val SetNoticeThread = SetNoticeThread(
        projectId = 1,
        threadId = 1,
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
