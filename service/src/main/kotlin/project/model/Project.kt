package waffle.guam.project.model

import waffle.guam.image.model.Image
import waffle.guam.project.ProjectEntity
import waffle.guam.stack.model.TechStack
import waffle.guam.task.model.Position
import waffle.guam.task.model.Task
import waffle.guam.task.model.UserState
import waffle.guam.user.model.User
import java.time.Instant

data class Project(
    val id: Long,
    val title: String,
    val description: String,
    val frontHeadCnt: Int,
    val backHeadCnt: Int,
    val designHeadCnt: Int,
    val state: ProjectState,
    val due: Due,
    val thumbnail: Image?,
    val techStacks: List<TechStack>?,
    val tasks: List<Task>?,
    val frontLeftCnt: Int?,
    val backLeftCnt: Int?,
    val designLeftCnt: Int?,
    val createdAt: Instant,
    val modifiedAt: Instant,
    val leaderProfile: User?
) {
    companion object {

        fun of(
            entity: ProjectEntity,
            techStacks: List<TechStack>? = null,
            tasks: List<Task>? = null
        ): Project {

            val currLeftCntOf = currLeftCntOf(tasks, entity)

            return Project(
                id = entity.id,
                title = entity.title,
                description = entity.description,
                frontHeadCnt = entity.frontHeadcount,
                backHeadCnt = entity.backHeadcount,
                designHeadCnt = entity.designerHeadcount,
                state = ProjectState.valueOf(entity.state),
                due = Due.valueOf(entity.due),
                // thumbnail = TODO("entity.thumbnail?.let { it.toDomain() }"),
                thumbnail = null,
                techStacks = techStacks,
                tasks = tasks,
                frontLeftCnt = currLeftCntOf?.get(Position.FRONTEND),
                backLeftCnt = currLeftCntOf?.get(Position.BACKEND),
                designLeftCnt = currLeftCntOf?.get(Position.DESIGNER),
                createdAt = entity.createdAt,
                modifiedAt = entity.modifiedAt,
                leaderProfile = tasks?.singleOrNull {
                    it.userState == UserState.LEADER
                }?.user
            )
        }

        private fun currLeftCntOf(tasks: List<Task>?, prj: ProjectEntity): Map<Position, Int>? {

            if (tasks == null) return null

            val res = mutableMapOf(
                Pair(Position.FRONTEND, prj.frontHeadcount), Pair(Position.BACKEND, prj.backHeadcount), Pair(Position.DESIGNER, prj.designerHeadcount)
            )

            tasks.filter {
                it.userState in listOf(UserState.LEADER, UserState.MEMBER)
            }.map {
                when (it.position) {
                    Position.WHATEVER -> 0
                    Position.DESIGNER -> res[Position.DESIGNER]!!.minus(1)
                    Position.BACKEND -> res[Position.BACKEND]!!.minus(1)
                    Position.FRONTEND -> res[Position.FRONTEND]!!.minus(1)
                }
            }
            return res
        }
    }
}
