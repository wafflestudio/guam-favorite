package waffle.guam.task

import org.springframework.data.jpa.repository.JpaRepository

interface TaskHistoryRepository : JpaRepository<TaskHistoryEntity, Long>
