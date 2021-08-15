package waffle.guam.user.event

import org.springframework.web.multipart.MultipartFile
import java.time.Instant

data class UserUpdated(
    val userId: Long,
    val willUploadImage: Boolean = false,
    val image: MultipartFile? = null,
    override val timestamp: Instant = Instant.now()
) : UserEvent(timestamp)
