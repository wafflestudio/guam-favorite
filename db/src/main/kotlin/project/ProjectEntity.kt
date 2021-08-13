package waffle.guam.project

import waffle.guam.image.ImageEntity
import waffle.guam.projectstack.ProjectStackEntity
import waffle.guam.task.TaskEntity
import java.time.Instant
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToMany
import javax.persistence.OneToOne
import javax.persistence.Table

@Table(name = "projects")
@Entity
data class ProjectEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    var title: String,

    var description: String,

    var frontHeadcount: Int,

    var backHeadcount: Int,

    var designerHeadcount: Int,

    val due: String,

    var state: String,

    val noticeThreadId: Long? = null,

    val createdAt: Instant = Instant.now(),

    var modifiedAt: Instant = createdAt,

    @OneToOne
    @JoinColumn(name = "thumbnail_id")
    var thumbnail: ImageEntity? = null,

    @OneToMany(mappedBy = "projectId")
    var techStacks: Set<ProjectStackEntity> = emptySet(),

    @OneToMany(mappedBy = "projectId")
    val tasks: Set<TaskEntity> = emptySet(),

)
