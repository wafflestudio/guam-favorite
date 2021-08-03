package waffle.guam.user.event

import java.time.Instant

data class UserUpdated(
    val userId: Long,
    override val timestamp: Instant = Instant.now()
) : UserEvent(timestamp)
