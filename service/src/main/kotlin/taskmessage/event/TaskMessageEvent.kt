package waffle.guam.taskmessage.event

import waffle.guam.GuamEvent
import java.time.Instant

abstract class TaskMessageEvent(open val timestamp: Instant) : GuamEvent
