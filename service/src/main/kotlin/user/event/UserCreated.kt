package waffle.guam.user.event

import java.time.Instant

data class UserCreated(
    val userId: Long,
    val firebaseUid: String,
    override val timestamp: Instant = Instant.now()
) : UserEvent(timestamp)
