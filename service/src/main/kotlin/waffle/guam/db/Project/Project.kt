package waffle.guam.db.Project

import org.springframework.data.annotation.CreatedDate
import waffle.guam.db.ProjectStack
import waffle.guam.db.Task
import java.time.LocalDateTime
import javax.persistence.*

@Table(name = "projects")
@Entity
data class Project(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = 0L,

    val title: String = "",

    val description: String = "",

    val thumbnail: String = "",

    @Column(name = "headcount_front")
    val headcount_front: Int = 0,
    @Column(name = "headcount_back")
    val headcount_back: Int = 0,
    @Column(name = "headcount_designer")
    val headcount_designer: Int = 0,

    @Column(name = "is_recruiting")
    val isRecruiting: Boolean = false,

    val created_at: LocalDateTime = LocalDateTime.now(),
    val modified_at: LocalDateTime = LocalDateTime.now(),

    @OneToMany( orphanRemoval = true, mappedBy = "project", cascade = [CascadeType.ALL])
    val techStacks: MutableList<ProjectStack> = ArrayList(),

    @OneToMany( orphanRemoval = true, mappedBy = "project_id", cascade = [CascadeType.ALL])
    val tasks: MutableList<Task> = ArrayList()
) {

}
