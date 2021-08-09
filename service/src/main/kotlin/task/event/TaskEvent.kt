package waffle.guam.task.event

import waffle.guam.GuamEvent
import java.time.Instant

abstract class TaskEvent(open val timestamp: Instant) : GuamEvent
