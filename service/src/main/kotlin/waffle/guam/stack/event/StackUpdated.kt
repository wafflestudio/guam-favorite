package waffle.guam.stack.event

import java.time.Instant

class StackUpdated(
    val stackId: Long,
    val stackName: String,
    override val timestamp: Instant = Instant.now()
) : StackEvent(timestamp) {
}