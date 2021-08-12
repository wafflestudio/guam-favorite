package waffle.guam.service.command

import waffle.guam.exception.InvalidRequestException

data class UpdateUser(
    val nickname: String? = null,
    val skills: List<String>? = null,
    val githubUrl: String? = null,
    val blogUrl: String? = null,
    val introduction: String? = null,
    val willUploadImage: Boolean
) {
    init {
        if (nickname?.isBlank() == true) {
            throw InvalidRequestException("유저 닉네임은 공백이 될 수 없습니다.")
        }
    }
}

data class UpdateFcmToken(
    val fcmToken: String
)
