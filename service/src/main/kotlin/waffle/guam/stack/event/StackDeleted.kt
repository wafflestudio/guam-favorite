package waffle.guam.stack.event

import java.time.Instant

class StackDeleted(
    val stackId: Long,
    override val timestamp: Instant = Instant.now()
) : StackEvent(timestamp)
