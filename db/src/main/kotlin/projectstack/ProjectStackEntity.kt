package waffle.guam.projectstack

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Table(name = "project_stacks")
@Entity
data class ProjectStackEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    val position: String,

    val projectId: Long,

    @Column(name = "tech_stack_id")
    val techStackId: Long
)
