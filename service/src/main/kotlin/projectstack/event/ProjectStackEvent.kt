package waffle.guam.projectstack.event

import waffle.guam.GuamEvent
import java.time.Instant

abstract class ProjectStackEvent(
    open val timestamp: Instant
) : GuamEvent
