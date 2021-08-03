package waffle.guam.thread.event

import org.springframework.web.multipart.MultipartFile

data class ThreadCreated(
    val threadId: Long,
    val imageFiles: List<MultipartFile>?,
) : ThreadEvent()
