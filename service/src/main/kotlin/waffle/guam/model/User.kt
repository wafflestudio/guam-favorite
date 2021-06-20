package waffle.guam.model

import waffle.guam.db.entity.UserEntity
import java.time.Instant

data class User(
    val id: Long,
    val status: String,
    val name: String,
    val imageUrl: String?,
    val skills: List<String>,
    val githubUrl: String?,
    val blogUrl: String?,
    val introduction: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    val isProfileSet: Boolean =
        name.isNotEmpty() && createdAt != updatedAt

    companion object {
        fun of(e: UserEntity): User =
            User(
                id = e.id,
                status = e.status.name,
                name = e.name,
                imageUrl = e.imageUrl,
                skills = e.skills?.split(",") ?: emptyList(),
                githubUrl = e.githubUrl,
                blogUrl = e.blogUrl,
                introduction = e.introduction,
                createdAt = e.createdAt,
                updatedAt = e.updatedAt
            )
    }
}
