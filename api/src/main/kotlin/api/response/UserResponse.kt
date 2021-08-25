package waffle.guam.api.response

import waffle.guam.user.model.User
import waffle.guam.user.model.UserProject
import waffle.guam.user.model.UserStatus
import java.time.Instant

data class UserResponse(
    val id: Long,
    val status: UserStatus,
    val nickname: String,
    val imageUrl: String?,
    val skills: List<String>,
    val githubUrl: String?,
    val blogUrl: String?,
    val introduction: String?,
    val projects: List<UserProject>?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val isProfileSet: Boolean
) {
    companion object {
        fun of(d: User) = UserResponse(
            id = d.id,
            status = d.status,
            nickname = d.nickname,
            imageUrl = d.image?.path,
            skills = d.skills,
            githubUrl = d.githubUrl,
            blogUrl = d.blogUrl,
            introduction = d.introduction,
            projects = d.projects,
            createdAt = d.createdAt,
            updatedAt = d.modifiedAt,
            isProfileSet = (d.createdAt != d.modifiedAt)
        )
    }
}
