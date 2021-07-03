package waffle.guam.model

import waffle.guam.db.entity.Position
import waffle.guam.db.entity.ProjectEntity
import waffle.guam.db.entity.ProjectView
import java.time.LocalDateTime

data class Project(
    val id: Long,
    val title: String,
    val description: String,
    val thumbnail: String?,
    val frontLeftCnt: Int,
    val backLeftCnt: Int,
    val designLeftCnt: Int,
    val isRecruiting: Boolean,
    val techStacks: List<TechStack>,
    val tasks: List<Task>?,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime,
) {
    companion object {
        fun of(entity: ProjectView, fetchTasks: Boolean = false): Project =
            Project(
                id = entity.id,
                title = entity.title,
                description = entity.description,
                thumbnail = entity.thumbnail,
                frontLeftCnt = entity.frontHeadcount,
                backLeftCnt = entity.backHeadcount,
                designLeftCnt = entity.designerHeadcount,
                isRecruiting = entity.recruiting,
                techStacks = entity.techStacks.map { TechStack.of(it.techStack) },
                tasks = when (fetchTasks) {
                    true -> entity.tasks.map { Task.of(it) }
                    else -> null
                },
                createdAt = entity.createdAt,
                modifiedAt = entity.modifiedAt
            )

        fun joinAs(position: Position, entity: ProjectEntity): ProjectEntity? {

//            when (position) {
//                Position.FRONTEND -> if( front-- < 0 ) throw JoinException("이 포지션에는 남는 자리가 없습니다.")
//                Position.BACKEND -> if ( back-- < 0 ) throw JoinException("이 포지션에는 남는 자리가 없습니다.")
//                Position.DESIGNER -> if ( design-- < 0 ) throw JoinException("이 포지션에는 남는 자리가 없습니다.")
//                Position.UNKNOWN -> throw JoinException("포지션을 정해 주세요.")
//            }
//            return ProjectEntity(
//                id = entity.id,
//                title = entity.title,
//                description = entity.description,
//                thumbnail = entity.thumbnail,
//                frontHeadcount = front, backHeadcount = back, designerHeadcount = design,
//                recruiting = if ( front + back + design == 0 ) false else entity.recruiting,
//                createdAt = entity.createdAt,
//                modifiedAt = LocalDateTime.now()
//            )
            return null
        }
    }
}
