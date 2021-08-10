package waffle.guam.image.event

import waffle.guam.GuamEvent
import java.time.Instant

abstract class ImageEvent(open val timestamp: Instant) : GuamEvent
