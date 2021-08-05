package waffle.guam.db.entity

import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.OneToOne
import javax.persistence.Table

sealed class ProjectModel

@Table(name = "projects")
@Entity
data class ProjectEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L,

    var title: String,

    var description: String,

    @OneToOne(fetch = FetchType.LAZY)
    var thumbnail: ImageEntity? = null,

    var frontHeadcount: Int,

    var backHeadcount: Int,

    var designerHeadcount: Int,

    @Enumerated(EnumType.STRING)
    var state: ProjectState = ProjectState.RECRUITING,

    var noticeThreadId: Long? = null,

    var createdAt: LocalDateTime = LocalDateTime.now(),

    var modifiedAt: LocalDateTime = createdAt,

    @Enumerated(EnumType.STRING)
    var due: Due
) : ProjectModel()

@Table(name = "projects")
@Entity
data class ProjectView(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    var title: String,

    var description: String,

    @OneToOne(fetch = FetchType.LAZY)
    var thumbnail: ImageEntity? = null,

    var frontHeadcount: Int,

    var backHeadcount: Int,

    var designerHeadcount: Int,

    @Enumerated(EnumType.STRING)
    var state: ProjectState,

    val noticeThreadId: Long?,

    val createdAt: LocalDateTime,

    var modifiedAt: LocalDateTime,

    @OneToMany(mappedBy = "projectId", fetch = FetchType.EAGER, orphanRemoval = true)
    var techStacks: Set<ProjectStackView>,

    @OneToMany(mappedBy = "projectId", fetch = FetchType.LAZY, orphanRemoval = true)
    val tasks: Set<TaskOverView>,

    @Enumerated(EnumType.STRING)
    val due: Due
) : ProjectModel()

@Table(name = "projects")
@Entity
data class ProjectDetailView(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    var title: String,

    var description: String,

    @OneToOne(fetch = FetchType.LAZY)
    var thumbnail: ImageEntity? = null,

    var frontHeadcount: Int,

    var backHeadcount: Int,

    var designerHeadcount: Int,

    @Enumerated(EnumType.STRING)
    var state: ProjectState,

    val noticeThreadId: Long?,

    val createdAt: LocalDateTime,

    var modifiedAt: LocalDateTime,

    @OneToMany(mappedBy = "projectId", fetch = FetchType.EAGER, orphanRemoval = true)
    var techStacks: Set<ProjectStackView>,

    @OneToMany(mappedBy = "projectId", fetch = FetchType.LAZY, orphanRemoval = true)
    val tasks: Set<TaskView>,

    @Enumerated(EnumType.STRING)
    val due: Due
) : ProjectModel()

enum class Due {
    ONE, THREE, SIX, MORE, UNDEFINED
}

enum class ProjectState {
    RECRUITING, ONGOING, PENDING, CLOSED
}
