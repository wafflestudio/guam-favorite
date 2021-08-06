package waffle.guam.project.model


import waffle.guam.db.entity.Due
import waffle.guam.db.entity.Position
import waffle.guam.db.entity.ProjectModel
import waffle.guam.db.entity.ProjectState
import waffle.guam.db.entity.ProjectView
import waffle.guam.db.entity.UserState
import waffle.guam.model.Image
import waffle.guam.model.Task
import waffle.guam.model.TechStack
import waffle.guam.model.User
import java.time.LocalDateTime

data class ProjectOverView(
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
) {
    companion object {

        fun of(
            entity: ProjectView,
            techStacks: List<TechStack>?,
            tasks: List<Task>?
        ): Project =
            currHeadCntOf(entity as ProjectView).let { arr ->
                Project(
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
                    techStacks = techStacks ?: emptyList(),
                    tasks = tasks,
                    createdAt = entity.createdAt,
                    modifiedAt = entity.modifiedAt,
                    due = entity.due,
                    leaderProfile = entity.tasks.singleOrNull {
                        it.userState == UserState.LEADER
                    }?.let {
                        User.of(it.user)
                    }
                )
            }

        private fun currHeadCntOf(projectView: ProjectModel): IntArray {
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
