package waffle.guam.taskmessage

import org.springframework.data.jpa.repository.JpaRepository

interface TaskMessageRepository : JpaRepository<TaskMessageEntity, Long>
