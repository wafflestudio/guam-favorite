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
import javax.persistence.Table
import javax.persistence.UniqueConstraint

@Table(
    name = "task_candidate",
    uniqueConstraints = [UniqueConstraint(name = "task_candidate_unique_key", columnNames = ["user_id", "project_id"])]
)
@Entity
data class TaskCandidateEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id")
    val project: ProjectEntity,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    val user: UserEntity,

    val position: String,

    val createdAt: Instant = Instant.now()
)
