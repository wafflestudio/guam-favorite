package waffle.guam.db.Project

import waffle.guam.db.DevType.TechStackDTO
import waffle.guam.db.Task
import java.time.LocalDateTime

data class ProjectDTO(
    var id: Long? = 0L,
    var title: String = "",
    var description: String = "",

    var thumbnail: String = "",

    var front_left: Int = 0,
    var back_left: Int = 0,
    var design_left: Int = 0,

    var isRecruiting: Boolean = false,

    var createdAt: LocalDateTime = LocalDateTime.now(),
    var modifiedAt: LocalDateTime = LocalDateTime.now(),

    // Entity 랑 Type 달라짐
    var techStacks: MutableList<TechStackDTO> = ArrayList(),
    val members: MutableList<Task> = ArrayList(),
) {

    companion object {
        fun of(e: Project): ProjectDTO {
            val l = e.techStacks.map { TechStackDTO.of( it.stack!! ) }
            return ProjectDTO(
                null, e.title, e.description, e.thumbnail, e.headcount_front, e.headcount_back, e.headcount_designer,
                e.isRecruiting, e.created_at, e.modified_at, l.toMutableList(), e.tasks
            )
        }
    }

    fun toEntity( ) : Project {
        return Project( this.id, this.title, this.description, this.thumbnail, this.front_left,
            this.back_left, this.design_left,
            this.isRecruiting, this.createdAt, this.modifiedAt )
    }

    fun update(p: ProjectReadDTO): ProjectDTO{
            title = p.title
            description = p.description
            thumbnail = p.thumbnail
            isRecruiting = p.isRecruiting
            front_left = p.front_left
            back_left = p.back_left
            design_left = p.design_left
            return this
    }

    fun create(p: ProjectCreateDTO): ProjectDTO{
        title = p.title
        description = p.description
        thumbnail = p.thumbnail
        isRecruiting = true
        front_left = p.front_left
        back_left = p.back_left
        design_left = p.design_left
        return this
    }

}
