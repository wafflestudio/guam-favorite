package waffle.guam.db.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Table(name = "project_stacks")
@Entity
data class ProjectStackEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Enumerated(EnumType.STRING)
    val position: Position = Position.UNKNOWN,

    val projectId: Long,

    @Column(name = "tech_stack_id")
    val techStackId: Long
)

@Table(name = "project_stacks")
@Entity
data class ProjectStackView(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Enumerated(EnumType.STRING)
    val position: Position,

    val projectId: Long,

    @ManyToOne
    @JoinColumn(name = "tech_stack_id")
    val techStack: TechStackEntity
)
