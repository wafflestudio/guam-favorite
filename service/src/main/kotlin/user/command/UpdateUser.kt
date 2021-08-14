package waffle.guam.user.command

import org.springframework.web.multipart.MultipartFile

data class UpdateUser(
    val nickname: String? = null,
    val skills: List<String>? = null,
    val githubUrl: String? = null,
    val blogUrl: String? = null,
    val introduction: String? = null,
    val willUploadImage: Boolean,
    val image: MultipartFile? = null,
) : UserCommand
