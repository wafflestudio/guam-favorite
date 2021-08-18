package waffle.guam.comment.event

import waffle.guam.GuamEvent
import java.time.Instant

abstract class CommentEvent(open val timestamp: Instant) : GuamEvent
