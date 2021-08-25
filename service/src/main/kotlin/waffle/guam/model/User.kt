package waffle.guam.model

import com.fasterxml.jackson.annotation.JsonFormat
import waffle.guam.db.entity.Position
import waffle.guam.db.entity.ProjectState
import waffle.guam.db.entity.TaskProjectView
import waffle.guam.db.entity.UserEntity
import java.time.Instant

data class User(
    val id: Long,
    val status: String,
    val nickname: String,
    val imageUrl: String?,
    val skills: List<String>,
    val githubUrl: String?,
    val blogUrl: String?,
    val introduction: String?,
    val projects: List<UserProject> = emptyList(),
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "Asia/Seoul")
    val createdAt: Instant,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "Asia/Seoul")
    val updatedAt: Instant,
) {
    val isProfileSet: Boolean =
        nickname.isNotEmpty() && createdAt != updatedAt

    companion object {
        fun of(e: UserEntity, filterGuestAndCompletedPrj: Boolean = true): User =
            User(
                id = e.id,
                status = e.status.name,
                nickname = e.nickname,
                imageUrl = e.image?.getPath(),
                skills = e.skills?.split(",") ?: emptyList(),
                githubUrl = e.githubUrl,
                blogUrl = e.blogUrl,
                introduction = e.introduction,
                projects = e.tasks.filter { it.userState.isValidMember(filterGuestAndCompletedPrj) }
                    .filter { it.project.state != ProjectState.CLOSED }
                    .filter { (filterGuestAndCompletedPrj || it.project.state != ProjectState.COMPLETED) }
                    .sortedByDescending { it.modifiedAt }
                    .map { UserProject.of(it) },
                createdAt = e.createdAt,
                updatedAt = e.updatedAt
            )
    }
}

data class UserProject(
    val projectId: Long,
    val projectTitle: String,
    val projectThumbnail: String?,
    val position: Position
) {
    companion object {
        fun of(e: TaskProjectView): UserProject =
            UserProject(
                projectId = e.project.id,
                projectTitle = e.project.title,
                projectThumbnail = e.project.thumbnail?.getPath(),
                position = e.position
            )
    }
}
