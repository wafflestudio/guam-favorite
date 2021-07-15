package waffle.guam.db.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import waffle.guam.db.entity.TaskMessage

interface TaskMessageRepository : JpaRepository<TaskMessage, Long> {

    fun findAllByTaskId(taskId: Long, pageable: Pageable): Page<TaskMessage>
}
