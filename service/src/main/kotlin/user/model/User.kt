package waffle.guam.user.model

import waffle.guam.image.model.Image
import waffle.guam.image.model.Image.Companion.toDomain
import waffle.guam.user.UserEntity
import java.time.Instant

data class User(
    val id: Long,
    val status: UserStatus,
    val nickname: String,
    val skills: List<String>,
    val githubUrl: String?,
    val blogUrl: String?,
    val introduction: String?,
    val image: Image? = null,
    val projects: List<UserProject>? = null,
    val createdAt: Instant,
    val modifiedAt: Instant,
) {
    companion object {
        fun of(e: UserEntity): User =
            User(
                id = e.id,
                status = UserStatus.valueOf(e.status),
                nickname = e.nickname,
                skills = e.skills?.split(",") ?: emptyList(),
                githubUrl = e.githubUrl,
                blogUrl = e.blogUrl,
                introduction = e.introduction,
                image = e.image?.toDomain(),
                createdAt = e.createdAt,
                modifiedAt = e.modifiedAt
            )
    }
}
