package waffle.guam.db.repository

import org.springframework.data.jpa.repository.JpaRepository
import waffle.guam.db.entity.Position
import waffle.guam.db.entity.State
import waffle.guam.db.entity.TaskEntity
import waffle.guam.db.entity.TaskView
import java.util.Optional

interface TaskRepository : JpaRepository<TaskEntity, Long> {

    fun findByUserId(userId: Long): List<TaskEntity>

    fun findByUserIdAndProjectId(userId: Long, projectId: Long): Optional<TaskEntity>

    fun findByUserIdAndProjectIdAndState(userId: Long, projectId: Long, state: State): Optional<TaskEntity>

    fun deleteByUserIdAndProjectId(userId: Long, projectId: Long): Unit

    fun countByUserIdAndStateNotLike(userId: Long, state: State = State.GUEST): Int

    fun countByProjectIdAndPosition(projectId: Long, position: Position): Int
}

interface TaskViewRepository : JpaRepository<TaskView, Long> {

    fun findByUserId(userId: Long): List<TaskView>

    fun findByUserIdAndProjectId(userId: Long, projectId: Long): Optional<TaskView>

    fun findByUserIdAndProjectIdAndState(userId: Long, projectId: Long, state: State): Optional<TaskView>

    fun deleteByUserIdAndProjectId(userId: Long, projectId: Long): Unit

    fun countByUserIdAndStateNotLike(userId: Long, state: State = State.GUEST): Int

    fun countByProjectIdAndPosition(projectId: Long, position: Position): Int
}
