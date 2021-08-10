package waffle.guam.user.model

import waffle.guam.user.UserEntity
import waffle.guam.user.command.UserExtraFieldParams
import java.time.Instant

data class User(
    val id: Long,
    val status: UserStatus,
    val nickname: String,
    val skills: List<String>,
    val githubUrl: String?,
    val blogUrl: String?,
    val introduction: String?,
    val imageUrl: String? = null,
    val projects: List<UserProject>? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        fun UserEntity.toDomain(extraFieldParams: UserExtraFieldParams = UserExtraFieldParams()): User {
            TODO()
        }
    }
}
