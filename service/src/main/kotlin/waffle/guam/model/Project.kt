package waffle.guam.model

import waffle.guam.db.entity.Due
import waffle.guam.db.entity.Position
import waffle.guam.db.entity.ProjectDetailView
import waffle.guam.db.entity.ProjectModel
import waffle.guam.db.entity.ProjectState
import waffle.guam.db.entity.ProjectView
import waffle.guam.db.entity.UserState
import java.time.LocalDateTime

interface ProjectInterface {

    fun of(
        entity: ProjectModel,
        fetchTasks: Boolean = false,
        thread: ThreadOverView? = null
    ): Project

    fun currHeadCntOf(projectView: ProjectModel): IntArray
}

sealed class Project

// TODO: DTO가 너무 더럽다.
data class ProjectList(
    val id: Long,
    val title: String,
    val description: String,
    val thumbnail: Image?,
    val frontHeadCnt: Int,
    val backHeadCnt: Int,
    val designHeadCnt: Int,
    val frontLeftCnt: Int,
    val backLeftCnt: Int,
    val designLeftCnt: Int,
    val state: ProjectState,
    val techStacks: List<TechStack>,
    val tasks: List<Task>?,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime,
    val due: Due,
    val leaderProfile: User?
) : Project() {
    companion object : ProjectInterface {

        override fun of(
            entity: ProjectModel,
            fetchTasks: Boolean,
            thread: ThreadOverView?
        ): ProjectList =
            currHeadCntOf(entity as ProjectView).let { arr ->
                ProjectList(
                    id = entity.id,
                    title = entity.title,
                    description = entity.description,
                    thumbnail =
                    if (entity.thumbnail != null) Image.of(entity.thumbnail!!)
                    else null,
                    frontHeadCnt = entity.frontHeadcount,
                    backHeadCnt = entity.backHeadcount,
                    designHeadCnt = entity.designerHeadcount,
                    frontLeftCnt = entity.frontHeadcount - arr[0],
                    backLeftCnt = entity.backHeadcount - arr[1],
                    designLeftCnt = entity.designerHeadcount - arr[2],
                    state = entity.state,
                    techStacks = entity.techStacks
                        .map { TechStack.of(it.techStack) }
                        .sortedByDescending { it.position },
                    tasks = when (fetchTasks) {
                        true -> entity.tasks.map { Task.of(it) }
                        else -> null
                    },
                    createdAt = entity.createdAt,
                    modifiedAt = entity.modifiedAt,
                    due = entity.due,
                    leaderProfile = when {
                        fetchTasks -> entity.tasks.singleOrNull {
                            it.userState == UserState.LEADER
                        }?.let {
                            User.of(it.user)
                        }
                        else -> null
                    }
                )
            }

        override fun currHeadCntOf(projectView: ProjectModel): IntArray {
            val res = MutableList(3, fun(_: Int) = 0)
            (projectView as ProjectView).tasks.filter {
                when (it.userState) {
                    UserState.LEADER, UserState.MEMBER -> true
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

data class ProjectDetail(
    val id: Long,
    val title: String,
    val description: String,
    val thumbnail: Image?,
    val frontHeadCnt: Int,
    val backHeadCnt: Int,
    val designHeadCnt: Int,
    val frontLeftCnt: Int,
    val backLeftCnt: Int,
    val designLeftCnt: Int,
    val state: ProjectState,
    val techStacks: List<TechStack>,
    val tasks: List<TaskOverview>?,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime,
    val due: Due,
    val leaderProfile: User?,
    val noticeThread: ThreadOverView?
) : Project() {
    companion object : ProjectInterface {
        override fun of(
            entity: ProjectModel,
            fetchTasks: Boolean,
            thread: ThreadOverView?
        ): ProjectDetail =
            currHeadCntOf(entity as ProjectDetailView).let { arr ->
                ProjectDetail(
                    id = entity.id,
                    title = entity.title,
                    description = entity.description,
                    thumbnail =
                    if (entity.thumbnail != null) Image.of(entity.thumbnail!!)
                    else null,
                    frontHeadCnt = entity.frontHeadcount,
                    backHeadCnt = entity.backHeadcount,
                    designHeadCnt = entity.designerHeadcount,
                    frontLeftCnt = entity.frontHeadcount - arr[0],
                    backLeftCnt = entity.backHeadcount - arr[1],
                    designLeftCnt = entity.designerHeadcount - arr[2],
                    state = entity.state,
                    techStacks = entity.techStacks
                        .map { TechStack.of(it.techStack) }
                        .sortedByDescending { it.position },
                    tasks = when (fetchTasks) {
                        true -> entity.tasks.map {
                            TaskOverview.of(it)
                        }
                        else -> null
                    },
                    createdAt = entity.createdAt,
                    modifiedAt = entity.modifiedAt,
                    due = entity.due,
                    leaderProfile = when {
                        fetchTasks -> entity.tasks.singleOrNull {
                            it.userState == UserState.LEADER
                        }?.let {
                            User.of(it.user)
                        }
                        else -> null
                    },
                    noticeThread = thread
                )
            }

        override fun currHeadCntOf(projectView: ProjectModel): IntArray {
            val res = MutableList(3, fun(_: Int) = 0)
            (projectView as ProjectDetailView).tasks.filter {
                when (it.userState) {
                    UserState.LEADER, UserState.MEMBER -> true
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
