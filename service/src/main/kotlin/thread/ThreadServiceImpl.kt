package waffle.guam.thread

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import waffle.guam.thread.command.CreateThread
import waffle.guam.thread.command.DeleteThread
import waffle.guam.thread.command.EditThreadContent
import waffle.guam.thread.command.RemoveNoticeThread
import waffle.guam.thread.command.SetNoticeThread
import waffle.guam.thread.event.NoticeThreadRemoved
import waffle.guam.thread.event.NoticeThreadSet
import waffle.guam.thread.event.ThreadContentEdited
import waffle.guam.thread.event.ThreadCreated
import waffle.guam.thread.event.ThreadDeleted
import waffle.guam.thread.model.ThreadDetail
import waffle.guam.thread.model.ThreadOverView

@Service
class ThreadServiceImpl(
    private val threadRepository: ThreadRepository
) : ThreadService {
    override fun getThreads(projectId: Long, pageable: Pageable): Page<ThreadOverView> {
        TODO("Not yet implemented")
    }

    override fun getFullThread(threadId: Long): ThreadDetail {
        TODO("Not yet implemented")
    }

    override fun setNoticeThread(command: SetNoticeThread): NoticeThreadSet {
        TODO("Not yet implemented")
    }

    override fun removeNoticeThread(command: RemoveNoticeThread): NoticeThreadRemoved {
        TODO("Not yet implemented")
    }

    override fun createThread(command: CreateThread): ThreadCreated {
        TODO("Not yet implemented")
    }

    override fun editThreadContent(command: EditThreadContent): ThreadContentEdited {
        TODO("Not yet implemented")
    }

    override fun deleteThread(command: DeleteThread): ThreadDeleted {
        TODO("Not yet implemented")
    }
}
