package waffle.guam.user.event

import java.time.Instant

data class DeviceUpdated(
    val userId: Long,
    val deviceId: String,
    override val timestamp: Instant = Instant.now()
) : UserEvent(timestamp)
