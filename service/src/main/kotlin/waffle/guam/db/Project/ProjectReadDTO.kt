package waffle.guam.db.Project

import waffle.guam.db.DevType.TechStackDTO
import waffle.guam.db.User.UserReadDTO
import java.time.LocalDateTime

// DTO for reading & passing information
// Id column excluded
data class ProjectReadDTO(
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
    var members: MutableList<UserReadDTO> = ArrayList(),
) {

    companion object {
        fun of(e: Project): ProjectReadDTO {
            return ProjectReadDTO(
                e.title, e.description, e.thumbnail, e.headcount_front, e.headcount_back, e.headcount_designer,
                e.isRecruiting, e.created_at, e.modified_at
            )
        }
    }

}
