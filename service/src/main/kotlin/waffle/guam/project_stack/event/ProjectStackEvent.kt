package waffle.guam.project_stack.event

import waffle.guam.event.GuamEvent
import java.time.Instant

abstract class ProjectStackEvent(
    open val timestamp: Instant
) : GuamEvent
