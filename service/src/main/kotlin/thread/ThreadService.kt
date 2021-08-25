package waffle.guam.thread

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import waffle.guam.thread.command.CreateJoinThread
import waffle.guam.thread.command.CreateThread
import waffle.guam.thread.command.DeleteThread
import waffle.guam.thread.command.EditJoinThreadType
import waffle.guam.thread.command.EditThreadContent
import waffle.guam.thread.command.SetNoticeThread
import waffle.guam.thread.event.JoinThreadCreated
import waffle.guam.thread.event.JoinThreadTypeEdited
import waffle.guam.thread.event.NoticeThreadSet
import waffle.guam.thread.event.ThreadContentEdited
import waffle.guam.thread.event.ThreadCreated
import waffle.guam.thread.event.ThreadDeleted
import waffle.guam.thread.model.ThreadDetail
import waffle.guam.thread.model.ThreadInfo
import waffle.guam.thread.model.ThreadOverView

interface ThreadService {
    fun getThread(threadId: Long): ThreadInfo
    fun getThreads(projectId: Long, pageable: Pageable): Page<ThreadOverView>
    fun getFullThread(threadId: Long): ThreadDetail
    fun setNoticeThread(command: SetNoticeThread): NoticeThreadSet
    fun createThread(command: CreateThread): ThreadCreated
    fun createJoinThread(command: CreateJoinThread): JoinThreadCreated
    fun editThreadContent(command: EditThreadContent): ThreadContentEdited
    // fun editThreadType(command: EditThreadType): ThreadTypeEdited
    fun editJoinThreadType(command: EditJoinThreadType): JoinThreadTypeEdited
    fun deleteThread(command: DeleteThread): ThreadDeleted
}
