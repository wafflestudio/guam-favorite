package waffle.guam.thread.event

import waffle.guam.GuamEvent
import java.time.Instant

abstract class ThreadEvent(open val timestamp: Instant) : GuamEvent
