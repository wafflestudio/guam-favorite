package waffle.guam.stack.event

import waffle.guam.event.GuamEvent
import java.time.Instant

abstract class StackEvent(
    open val timestamp: Instant
) : GuamEvent
