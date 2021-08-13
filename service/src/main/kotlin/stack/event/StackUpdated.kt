package waffle.guam.stack.event

import org.springframework.web.multipart.MultipartFile
import java.time.Instant

class StackUpdated(
    val stackId: Long,
    val stackName: String,
    val imageFiles: MultipartFile? = null,
    override val timestamp: Instant = Instant.now()
) : StackEvent(timestamp)
