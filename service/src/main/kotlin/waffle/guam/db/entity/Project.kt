package waffle.guam.db.entity

import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table

@Table(name = "projects")
@Entity
data class ProjectEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    val title: String,

    val description: String,

    val thumbnail: String,

    val frontHeadcount: Int,

    val backHeadcount: Int,

    val designerHeadcount: Int,

    val isRecruiting: Boolean = true,

    val createdAt: LocalDateTime = LocalDateTime.now(),

    val modifiedAt: LocalDateTime = createdAt
)

@Table(name = "projects")
@Entity
data class ProjectView(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    val title: String,

    val description: String,

    val thumbnail: String,

    val frontHeadcount: Int,

    val backHeadcount: Int,

    val designerHeadcount: Int,

    val isRecruiting: Boolean,

    val createdAt: LocalDateTime,

    val modifiedAt: LocalDateTime,

    @OneToMany(mappedBy = "projectId", fetch = FetchType.EAGER)
    val techStacks: Set<ProjectStackView>,

    @OneToMany(mappedBy = "projectId", fetch = FetchType.LAZY)
    val tasks: Set<TaskView>,
)
