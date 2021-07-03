package waffle.guam.controller.request

data class Content(
    val value: String
)

data class CreateChatInput(
    val content: String? = null,
    val imageUrls: List<String>? = null
)

data class DeleteImageInput(
    val imageUrl: String
)
