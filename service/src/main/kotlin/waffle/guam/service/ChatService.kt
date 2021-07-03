package waffle.guam.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import waffle.guam.db.entity.CommentEntity
import waffle.guam.db.entity.ImageEntity
import waffle.guam.db.entity.ImageType
import waffle.guam.db.repository.ThreadRepository
import waffle.guam.db.repository.ImageRepository
import waffle.guam.db.repository.CommentRepository
import waffle.guam.db.repository.ProjectRepository
import waffle.guam.db.repository.ThreadViewRepository
import waffle.guam.exception.DataNotFoundException
import waffle.guam.exception.InvalidRequestException
import waffle.guam.model.Comment
import waffle.guam.model.Image
import waffle.guam.model.ThreadDetail
import waffle.guam.model.ThreadOverView
import waffle.guam.service.command.*
import java.time.LocalDateTime

@Service
class ChatService(
    private val threadRepository: ThreadRepository,
    private val threadViewRepository: ThreadViewRepository,
    private val commentRepository: CommentRepository,
    private val projectRepository: ProjectRepository,
    private val imageRepository: ImageRepository
) {

    fun getThreads(projectId: Long, pageable: Pageable): Page<ThreadOverView> {
        val foundUserProfiles : HashMap<Long, String> = HashMap()
        return threadViewRepository.findByProjectId(projectId, pageable).map {
            ThreadOverView.of(
                it,
                { creatorId ->
                    if(!foundUserProfiles.containsKey(creatorId))
                        foundUserProfiles[creatorId] = imageRepository.findByParentIdAndType(creatorId, ImageType.USER_PROFILE)[0].url
                    foundUserProfiles[creatorId]
                },
                { threadId -> commentRepository.countByThreadId(threadId) },
                { threadId -> imageRepository.findByParentIdAndType(threadId, ImageType.THREAD).map { image -> Image.of(image) } }
            )
        }
    }

    fun getFullThread(threadId: Long): ThreadDetail {
        val foundUserProfiles : HashMap<Long, String> = HashMap()
        return threadViewRepository.findById(threadId).orElseThrow(::RuntimeException).let { threadView ->
            ThreadDetail.of(
                threadView,
                { creatorId ->
                    foundUserProfiles[creatorId] = imageRepository.findByParentIdAndType(creatorId, ImageType.USER_PROFILE)[0].url
                    foundUserProfiles[creatorId]
                },
                { threadId -> imageRepository.findByParentIdAndType(threadId, ImageType.THREAD).map { image -> Image.of(image) } },
                comments = threadView.comments.map { Comment.of(
                    it,
                    { creatorId ->
                        if(!foundUserProfiles.containsKey(creatorId))
                            foundUserProfiles[creatorId] = imageRepository.findByParentIdAndType(creatorId, ImageType.USER_PROFILE)[0].url
                        foundUserProfiles[creatorId]
                    },
                    { commentId -> imageRepository.findByParentIdAndType(commentId, ImageType.THREAD).map { image -> Image.of(image) } },
                    )
                },
            )
        }
    }

    @Transactional
    fun createThread(command: CreateThread): Boolean {
        if (command.content == null && command.imageUrls == null) throw InvalidRequestException("입력된 내용이 없습니다.")
        projectRepository.findById(command.projectId).orElseThrow(::DataNotFoundException)

        if (command.imageUrls != null)
            for (imageUrl in command.imageUrls)
                imageRepository.save(ImageEntity(type = ImageType.THREAD, parentId = command.projectId, url = imageUrl))
        threadRepository.save(command.toEntity())
        return true
    }

    @Transactional
    fun editThreadContent(command: EditThreadContent): Boolean {
        threadRepository.findById(command.threadId).orElseThrow(::DataNotFoundException).let {
            if (it.userId != command.userId) throw InvalidRequestException()
            if (it.content == command.content) throw InvalidRequestException("수정 전과 동일한 내용입니다.")

            threadRepository.save(
                it.copy(content = command.content, modifiedAt = LocalDateTime.now())
            )
        }
        return true
    }

    @Transactional
    fun deleteThreadImage(command: DeleteThreadImage): Boolean {
        val threadCreatorId = threadRepository.findById(command.threadId).orElseThrow(::DataNotFoundException).userId

        imageRepository.findById(command.imageId).orElseThrow(::DataNotFoundException).let {
            if (threadCreatorId != command.userId) throw InvalidRequestException()
            imageRepository.delete(it)
        }
        return true
    }

    @Transactional
    fun deleteThread(command: DeleteThread): Boolean {
        threadRepository.findById(command.threadId).orElseThrow(::DataNotFoundException).let { threadEntity ->
            if (threadEntity.userId != command.userId) throw InvalidRequestException()
            val childComments: List<CommentEntity> = commentRepository.findByThreadId(command.threadId)

            if (childComments.isNotEmpty()) {
                imageRepository.deleteByParentIdInAndType(childComments.map { it.id }, ImageType.COMMENT)
                commentRepository.deleteAll(childComments)
            }
            imageRepository.deleteByParentIdAndType(threadEntity.id, ImageType.THREAD)
            threadRepository.delete(threadEntity)
        }
        return true
    }

    @Transactional
    fun createComment(command: CreateComment): Boolean {
        if (command.content == null && command.imageUrls == null) throw InvalidRequestException("입력된 내용이 없습니다.")
        threadRepository.findById(command.threadId).orElseThrow(::DataNotFoundException)

        if (command.imageUrls != null)
            for (imageUrl in command.imageUrls)
                imageRepository.save(ImageEntity(type = ImageType.COMMENT, parentId = command.threadId, url = imageUrl))
        commentRepository.save(command.toEntity())
        return true
    }

    @Transactional
    fun editCommentContent(command: EditCommentContent): Boolean {
        commentRepository.findById(command.commentId).orElseThrow(::DataNotFoundException).let {
            if (it.userId != command.userId) throw InvalidRequestException()
            if (it.content == command.content) throw InvalidRequestException("수정 전과 동일한 내용입니다.")

            commentRepository.save(
                it.copy(content = command.content, modifiedAt = LocalDateTime.now())
            )
        }
        return true
    }

    @Transactional
    fun deleteCommentImage(command: DeleteCommentImage): Boolean {
        val commentCreatorId = commentRepository.findById(command.commentId).orElseThrow(::DataNotFoundException).userId

        imageRepository.findById(command.imageId).orElseThrow(::DataNotFoundException).let {
            if (commentCreatorId != command.userId) throw InvalidRequestException()
            imageRepository.delete(it)
        }
        return true
    }

    @Transactional
    fun deleteComment(command: DeleteComment): Boolean {
        commentRepository.findById(command.commentId).orElseThrow(::DataNotFoundException).let {
            if (it.userId != command.userId) throw InvalidRequestException()
            imageRepository.deleteByParentIdAndType(command.commentId, ImageType.COMMENT)
            commentRepository.delete(it)
        }
        return true
    }
}
