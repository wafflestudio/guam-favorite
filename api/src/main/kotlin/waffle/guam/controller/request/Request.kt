package waffle.guam.controller.request

data class EditChatInput(
    val content: String
)

data class CreateChatInput(
    val content: String? = null,
    val imageUrls: List<String>? = null
)
