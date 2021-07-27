package waffle.guam.model

data class PushRequest(
    val validate_only: Boolean,
    val message: Message
)

data class Message(
    private val notification: Notification,
    private val token: String
)

class Notification(
    private val title: String? = null,
    private val body: String? = null,
    private val image: String? = null,
)
