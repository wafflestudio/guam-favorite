package waffle.guam.event

import java.time.Instant

abstract class ThreadEvent(open val timestamp: Instant) : GuamEvent
