package waffle.guam.db.Project

import waffle.guam.db.DevType.TechStack
import waffle.guam.db.DevType.TechStackDTO
import waffle.guam.db.Task
import waffle.guam.db.ProjectStack
import waffle.guam.db.User.UserDTO
import java.time.LocalDateTime

data class ProjectCreateDTO(
    var title: String = "",
    var description: String = "",

    var thumbnail: String = "",

    var front_left: Int = 0,
    var back_left: Int = 0,
    var design_left: Int = 0,

    var techStacks: MutableList<TechStackDTO> = ArrayList(),

    // project owner's position on this project
    //var user_position: Int = 0
) {

}
