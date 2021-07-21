package waffle.guam.model

import waffle.guam.db.entity.Due
import waffle.guam.db.entity.Position
import waffle.guam.db.entity.ProjectView
import waffle.guam.db.entity.State
import java.time.LocalDateTime

// TODO: DTO가 너무 더럽다.
data class Project(
    val id: Long,
    val title: String,
    val description: String,
    val thumbnail: String?,
    val frontHeadCnt: Int,
    val backHeadCnt: Int,
    val designHeadCnt: Int,
    val frontLeftCnt: Int,
    val backLeftCnt: Int,
    val designLeftCnt: Int,
    val isRecruiting: Boolean,
    val techStacks: List<TechStack>,
    val tasks: List<Task>?,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime,
    val due: Due,
    val leaderProfile: User?,
    val noticeThread: ThreadOverView?
) {
    companion object {
        fun of(
            entity: ProjectView,
            fetchTasks: Boolean = false,
            thread: ThreadOverView? = null
        ): Project =
            currHeadCntOf(entity).let { arr ->
                Project(
                    id = entity.id,
                    title = entity.title,
                    description = entity.description,
                    thumbnail = entity.thumbnail,
                    frontHeadCnt = entity.frontHeadcount,
                    backHeadCnt = entity.backHeadcount,
                    designHeadCnt = entity.designerHeadcount,
                    frontLeftCnt = entity.frontHeadcount - arr[0],
                    backLeftCnt = entity.backHeadcount - arr[1],
                    designLeftCnt = entity.designerHeadcount - arr[2],
                    isRecruiting = entity.recruiting,
                    techStacks = entity.techStacks.map { TechStack.of(it.techStack) },
                    tasks = when (fetchTasks) {
                        true -> entity.tasks.map { Task.of(it) }
                        else -> null
                    },
                    createdAt = entity.createdAt,
                    modifiedAt = entity.modifiedAt,
                    due = entity.due,
                    leaderProfile = when (fetchTasks) {
                        true -> entity.tasks.single {
                            it.state == State.LEADER
                        }.let {
                            User.of(it.user)
                        }
                        else -> null
                    },
                    noticeThread = thread
                )
            }

        private fun currHeadCntOf(projectView: ProjectView): IntArray {
            val res = MutableList(3, fun(_: Int) = 0)
            projectView.tasks.filter {
                when (it.state) {
                    State.LEADER, State.MEMBER -> true
                    else -> false
                }
            }.map {
                when (it.position) {
                    Position.WHATEVER -> 0
                    Position.DESIGNER -> res[2]++
                    Position.BACKEND -> res[1]++
                    Position.FRONTEND -> res[0]++
                }
            }
            return res.toIntArray()
        }
    }
}
