package waffle.guam.service.command

data class UpdateUser(
    val nickname: String? = null,
    val skills: String? = null,
    val githubUrl: String? = null,
    val blogUrl: String? = null,
    val introduction: String? = null
)
