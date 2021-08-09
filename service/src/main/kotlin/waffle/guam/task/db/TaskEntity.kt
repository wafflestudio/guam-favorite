package waffle.guam.task.db

import waffle.guam.taskmessage.db.TaskMessageEntity
import waffle.guam.user.db.UserEntity
import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.Table

@Table(name = "tasks")
@Entity
data class TaskEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Enumerated(EnumType.STRING)
    val position: Position,

    @Column(name = "project_id")
    val projectId: Long,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    val user: UserEntity,

    @Enumerated(EnumType.STRING)
    var userState: UserState,

    @OneToMany
    val taskMessages: Set<TaskMessageEntity> = emptySet(),

    val createdAt: Instant = Instant.now(),

    val modifiedAt: Instant = createdAt,
)
