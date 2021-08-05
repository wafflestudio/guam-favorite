package waffle.guam.user.model

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
)
