package waffle.guam.db

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import waffle.guam.db.DevType.TechStack
import waffle.guam.db.Project.Project
import javax.persistence.*

@Table(name = "project_stacks")
@Entity
data class ProjectStack(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    val position_info: Int = 0,

    @ManyToOne
    @JoinColumn(name = "project_id")
    var project: Project = Project(),

    @ManyToOne(cascade = [CascadeType.MERGE])
    @JoinColumn(name = "stack_id" )
    val stack: TechStack = TechStack()
){

}