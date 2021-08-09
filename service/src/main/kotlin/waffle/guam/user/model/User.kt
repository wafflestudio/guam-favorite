package waffle.guam.user.model

import waffle.guam.user.db.UserEntity
import java.time.Instant

data class User(
    val id: Long,
    val status: String,
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
        fun UserEntity.toDomain() = User(
            id = id,
            status = status.name,
            nickname = nickname,
            skills = skills?.split(",") ?: emptyList(),
            githubUrl = githubUrl,
            blogUrl = blogUrl,
            introduction = introduction,
            imageUrl = image?.getPath(),
            projects = null,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
