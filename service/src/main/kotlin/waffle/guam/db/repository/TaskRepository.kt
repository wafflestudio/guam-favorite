package waffle.guam.db.repository

import org.springframework.data.jpa.repository.JpaRepository
import waffle.guam.db.entity.Position
import waffle.guam.db.entity.TaskEntity
import waffle.guam.db.entity.TaskProjectView
import waffle.guam.db.entity.TaskView
import waffle.guam.db.entity.UserState
import java.util.Optional

interface TaskRepository : JpaRepository<TaskEntity, Long> {

    fun findByUserId(userId: Long): Optional<List<TaskEntity>>

    fun findByUserIdAndProjectId(userId: Long, projectId: Long): Optional<TaskEntity>

    fun findByUserIdAndProjectIdAndUserState(userId: Long, projectId: Long, userState: UserState): Optional<TaskEntity>

    fun deleteByUserIdAndProjectId(userId: Long, projectId: Long): Unit

    fun countByUserIdAndUserStateNotIn(
        userId: Long,
        userState: Array<UserState> = arrayOf(UserState.DECLINED, UserState.QUIT)
    ): Int

    fun countByProjectIdAndPosition(projectId: Long, position: Position): Int
}

interface TaskViewRepository : JpaRepository<TaskView, Long> {

    fun findByUserId(userId: Long): List<TaskView>

    fun findByUserIdAndProjectId(userId: Long, projectId: Long): Optional<TaskView>

    fun findByUserIdAndProjectIdAndUserState(userId: Long, projectId: Long, userState: UserState): Optional<TaskView>

    fun deleteByUserIdAndProjectId(userId: Long, projectId: Long): Unit

    fun countByUserIdAndUserStateNotLike(userId: Long, userState: UserState = UserState.GUEST): Int

    fun countByProjectIdAndPosition(projectId: Long, position: Position): Int
}

interface TaskProjectViewRepository : JpaRepository<TaskProjectView, Long> {
    fun findByUserId(userId: Long): List<TaskProjectView>
}
