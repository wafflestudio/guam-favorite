package waffle.guam.task

import waffle.guam.project.ProjectEntity
import waffle.guam.user.UserEntity
import java.time.Instant
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToOne
import javax.persistence.Table

@Table(name = "task_history")
@Entity
data class TaskHistoryEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    val project: ProjectEntity,

    @OneToOne
    @JoinColumn(name = "user_id")
    val user: UserEntity,

    var userState: String,

    val position: String,

    val description: String = "QUIT",

    val createdAt: Instant = Instant.now()
)
