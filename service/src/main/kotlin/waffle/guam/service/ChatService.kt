package waffle.guam.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import waffle.guam.db.repository.CommentRepository
import waffle.guam.db.repository.ThreadRepository
import waffle.guam.db.repository.ThreadViewRepository
import waffle.guam.exception.DataNotFoundException
import waffle.guam.exception.InvalidRequestException
import waffle.guam.model.ThreadDetail
import waffle.guam.model.ThreadOverView
import waffle.guam.service.command.CreateComment
import waffle.guam.service.command.CreateThread
import waffle.guam.service.command.DeleteComment
import waffle.guam.service.command.DeleteThread
import waffle.guam.service.command.EditComment
import waffle.guam.service.command.EditThread
import java.time.LocalDateTime

@Service
class ChatService(
    private val threadRepository: ThreadRepository,
    private val threadViewRepository: ThreadViewRepository,
    private val commentRepository: CommentRepository,
) {

    fun getThreads(projectId: Long, pageable: Pageable): Page<ThreadOverView> =
        threadViewRepository.findByProjectId(projectId, pageable).map {
            ThreadOverView.of(it) { threadId ->
                commentRepository.countByThreadId(threadId)
            }
        }

    fun getFullThread(threadId: Long): ThreadDetail =
        threadViewRepository.findById(threadId).orElseThrow(::RuntimeException).let { ThreadDetail.of(it) }

    @Transactional
    fun createThread(command: CreateThread): Boolean {
        threadRepository.save(command.toEntity())
        return true
    }

    @Transactional
    fun editThread(command: EditThread): Boolean {
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
    fun deleteThread(command: DeleteThread): Boolean {
        threadRepository.findById(command.threadId).orElseThrow(::DataNotFoundException).let {
            if (it.userId != command.userId) throw InvalidRequestException()
            threadRepository.delete(it)
            commentRepository.removeByThreadId(it.id)
        }
        return true
    }

    @Transactional
    fun createComment(command: CreateComment): Boolean {
        threadRepository.findById(command.threadId).orElseThrow(::DataNotFoundException)
        commentRepository.save(command.toEntity())
        return true
    }

    @Transactional
    fun editComment(command: EditComment): Boolean {
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
    fun deleteComment(command: DeleteComment): Boolean {
        commentRepository.findById(command.commentId).orElseThrow(::DataNotFoundException).let {
            if (it.userId != command.userId) throw InvalidRequestException()
            commentRepository.delete(it)
        }
        return true
    }
}
