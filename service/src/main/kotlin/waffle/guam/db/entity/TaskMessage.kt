package waffle.guam.db.entity

import java.time.LocalDateTime
import javax.persistence.Table
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.GenerationType
import javax.persistence.GeneratedValue

@Table(name = "task_msg")
@Entity
data class TaskMessage (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    val msg: String? = "New task msg",

    val status: TaskStatus? = TaskStatus.ONGOING,

    val taskId: Long,

    val createdAt: LocalDateTime = LocalDateTime.now(),

    val modifiedAt: LocalDateTime = createdAt

    ){

}

enum class TaskStatus {
    DONE, ONGOING
}
