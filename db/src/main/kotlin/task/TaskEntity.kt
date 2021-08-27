package waffle.guam.task

import waffle.guam.project.ProjectEntity
import waffle.guam.taskmessage.TaskMessageEntity
import waffle.guam.user.UserEntity
import java.time.Instant
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
    uniqueConstraints = [UniqueConstraint(name = "tasks_unique_key", columnNames = ["user_id", "project_id"])]
)
@Entity
data class TaskEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    val project: ProjectEntity,

    @OneToOne
    @JoinColumn(name = "user_id")
    var user: UserEntity? = null,

    var userState: String? = null,

    val position: String,

    @OneToMany(mappedBy = "taskId")
    val taskMessages: Set<TaskMessageEntity> = emptySet(),

    val createdAt: Instant = Instant.now(),

    val modifiedAt: Instant = createdAt,
)
