package waffle.guam.user.event

import waffle.guam.GuamEvent
import java.time.Instant

abstract class UserEvent(open val timestamp: Instant) : GuamEvent
