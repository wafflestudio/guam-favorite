package waffle.guam.controller.request

import org.springframework.web.multipart.MultipartFile

data class ContentInput(
    val content: String
)

data class CreateFullInfoInput(
    val content: String?,
    val files: List<MultipartFile>?
)
