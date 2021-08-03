package waffle.guam.task.event

import waffle.guam.event.GuamEvent
import java.time.Instant

abstract class TaskEvent(open val timestamp: Instant) : GuamEvent