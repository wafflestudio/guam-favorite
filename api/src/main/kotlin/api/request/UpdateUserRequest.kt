package waffle.guam.api.request

import org.springframework.web.multipart.MultipartFile
import waffle.guam.user.command.UpdateUser

data class UpdateUserRequest(
    val nickname: String? = null,
    val skills: List<String>? = null,
    val githubUrl: String? = null,
    val blogUrl: String? = null,
    val introduction: String? = null,
    val willUploadImage: Boolean,
) {
    fun toCommand(file: MultipartFile?) = UpdateUser(
        nickname = nickname,
        skills = skills,
        githubUrl = githubUrl,
        blogUrl = blogUrl,
        introduction = introduction,
        willUploadImage = willUploadImage,
        image = file
    )
}
