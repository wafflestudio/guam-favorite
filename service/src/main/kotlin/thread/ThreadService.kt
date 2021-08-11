package waffle.guam.thread

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import waffle.guam.thread.command.CreateThread
import waffle.guam.thread.command.DeleteThread
import waffle.guam.thread.command.DeleteThreadImage
import waffle.guam.thread.command.EditThreadContent
import waffle.guam.thread.command.GetThreadInfo
import waffle.guam.thread.command.RemoveNoticeThread
import waffle.guam.thread.command.SetNoticeThread
import waffle.guam.thread.event.NoticeThreadRemoved
import waffle.guam.thread.event.NoticeThreadSet
import waffle.guam.thread.event.ThreadContentEdited
import waffle.guam.thread.event.ThreadCreated
import waffle.guam.thread.event.ThreadDeleted
import waffle.guam.thread.event.ThreadImageDeleted
import waffle.guam.thread.model.ThreadDetail
import waffle.guam.thread.model.ThreadOverView

interface ThreadService {
    fun getThreads(projectId: Long, pageable: Pageable): Page<ThreadOverView>
    fun getFullThread(threadId: Long): ThreadDetail
    fun getThreadInfo(command: GetThreadInfo): List<ThreadEntity>
    fun setNoticeThread(command: SetNoticeThread): NoticeThreadSet
    fun removeNoticeThread(command: RemoveNoticeThread): NoticeThreadRemoved
    fun createThread(command: CreateThread): ThreadCreated
    fun editThreadContent(command: EditThreadContent): ThreadContentEdited
    fun deleteThreadImage(command: DeleteThreadImage): ThreadImageDeleted
    fun deleteThread(command: DeleteThread): ThreadDeleted
}
