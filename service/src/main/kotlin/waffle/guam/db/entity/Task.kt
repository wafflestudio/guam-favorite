package waffle.guam.db.entity

import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToMany
import javax.persistence.OneToOne
import javax.persistence.Table

@Table(name = "tasks")
@Entity
data class TaskEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Enumerated(EnumType.STRING)
    val position: Position,

    val projectId: Long,

    @Column(name = "user_id")
    val userId: Long,

    val createdAt: LocalDateTime = LocalDateTime.now(),

    val modifiedAt: LocalDateTime = createdAt,

    @Enumerated(EnumType.STRING)
    val state: State
)

@Table(name = "tasks")
@Entity
data class TaskView(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Enumerated(EnumType.STRING)
    val position: Position,

    @OneToMany(mappedBy = "taskId", fetch = FetchType.LAZY, orphanRemoval = true)
    val tasks: Set<TaskMessage>,

    val projectId: Long,

    @OneToOne
    @JoinColumn(name = "user_id")
    val user: UserEntity,

    val createdAt: LocalDateTime,

    val modifiedAt: LocalDateTime,

    @Enumerated(EnumType.STRING)
    val state: State
)

enum class Position {
    FRONTEND, BACKEND, DESIGNER, WHATEVER
}

enum class State {
    GUEST, MEMBER, LEADER, QUIT, DECLINED
}
