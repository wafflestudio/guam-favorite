package waffle.guam.project.event

import waffle.guam.GuamEvent
import java.time.Instant

abstract class ProjectEvent(
    open val timestamp: Instant
) : GuamEvent
