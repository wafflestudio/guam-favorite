package waffle.guam.event

import java.time.Instant

abstract class ProjectEvent(open val timestamp: Instant) : GuamEvent
