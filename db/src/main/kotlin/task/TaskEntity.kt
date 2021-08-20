package waffle.guam.task

import waffle.guam.project.ProjectEntity
import waffle.guam.taskmessage.TaskMessageEntity
import waffle.guam.user.UserEntity
import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.OneToOne
import javax.persistence.Table
import javax.persistence.UniqueConstraint

@Table(
    name = "tasks",
    uniqueConstraints = [
        UniqueConstraint(name = "user_project", columnNames = ["user_id", "project_id"]),
        UniqueConstraint(name = "user_offset", columnNames = ["user_id", "user_offset"]),
        UniqueConstraint(name = "project_position_offset", columnNames = ["project_id", "position", "position_offset"]),
    ]
)
@Entity
data class TaskEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    val project: ProjectEntity,

    val position: String,

    @OneToMany(mappedBy = "taskId")
    val taskMessages: Set<TaskMessageEntity> = emptySet(),

    @OneToOne
    @JoinColumn(name = "user_id")
    val user: UserEntity,

    var userState: String,

    @Column(name = "user_offset")
    var userOffset: Int = 0,

    @Column(name = "position_offset")
    var positionOffset: Int? = null,

    val createdAt: Instant = Instant.now(),

    val modifiedAt: Instant = createdAt,
)
